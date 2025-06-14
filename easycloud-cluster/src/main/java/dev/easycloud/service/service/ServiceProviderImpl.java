package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.network.event.resources.ServiceStartingEvent;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.scheduler.EasyScheduler;
import dev.easycloud.service.service.builder.ServiceLaunchFactory;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import dev.easycloud.service.service.listener.*;
import dev.easycloud.service.service.resources.*;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceProviderImpl implements ServiceProvider {
    @Getter
    private final List<Service> services = new ArrayList<>();

    public ServiceProviderImpl() {
        new EasyScheduler(this::refresh).repeat(TimeUnit.SECONDS.toMillis(5));

        var templatePath = Path.of("local").resolve("templates");
        //noinspection ResultOfMethodCallIgnored
        templatePath.resolve("global").resolve("all").toFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        templatePath.resolve("global").resolve("server").toFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        templatePath.resolve("global").resolve("proxy").toFile().mkdirs();

        new ServiceReadyListener();
        new ServiceShutdownListener();
        new ServiceRequestInformationListener();
        new ServiceRequestLaunchListener();
        new ServiceUpdateListener();
        new ServiceRequestShutdownListener();
    }

    public void refresh() {
        for (ServiceImpl service : this.services.stream().map(it -> (ServiceImpl) it).filter(it -> it.process() == null || !it.process().isAlive()).toList()) {
            this.shutdown(service);
        }

        if (EasyCloudCluster.instance().groupProvider() == null) {
            return;
        }

        var currentStarting = this.services.stream().filter(it -> it.state().equals(ServiceState.STARTING)).count();
        if (currentStarting >= EasyCloudCluster.instance().configuration().local().startingSameTime()) {
            return;
        }

        for (Group group : EasyCloudCluster.instance().groupProvider().groups()
                .stream()
                .sorted((o1, o2) -> {
                    if(o1.property(GroupProperties.PRIORITY()) == null) {
                        log.error("Group {} has no priority set, defaulting to 0.", o1.name());
                        return 0;
                    }
                    if(o2.property(GroupProperties.PRIORITY()) == null) {
                        log.error("Group {} has no priority set, defaulting to 0.", o2.name());
                        return 0;
                    }
                    return Integer.compare(o2.property(GroupProperties.PRIORITY()), o1.property(GroupProperties.PRIORITY()));
                })
                .filter(Group::enabled)
                .toList()) {
            var always = group.property(GroupProperties.ALWAYS_RUNNING());
            var max = group.property(GroupProperties.MAXIMUM_RUNNING());
            if(max == -1) {
                max = Integer.MAX_VALUE;
            }
            var online = this.services.stream().filter(it -> it.group().name().equals(group.name())).count();
            if (always > online && online < max) {
                for (int i = 0; i < always - online; i++) {
                    if(this.services.stream().filter(it -> it.group().name().equals(group.name())).count() >= max) {
                        break;
                    }
                    if (currentStarting >= EasyCloudCluster.instance().configuration().local().startingSameTime()) {
                        return;
                    }
                    this.launch(new ServiceLaunchBuilder(group.name()));
                    currentStarting++;
                }
            }

            online = this.services.stream().filter(it -> it.group().name().equals(group.name())).count();
            var percentage = EasyCloudCluster.instance().configuration().local().dynamicPercentage() / 100.0;

            if(online < max) {
                for (Service service : this.services().stream().filter(it -> it.group().name().equals(group.name())).toList()) {
                    if(service.property("ALREADY_LAUNCHED", Boolean.class) != null && service.property("ALREADY_LAUNCHED", Boolean.class)) {
                        continue;
                    }

                    if (service.state().equals(ServiceState.ONLINE)) {
                        if(((double) service.property(ServiceProperties.ONLINE_PLAYERS()) / group.property(GroupProperties.MAX_PLAYERS())) >= percentage) {
                            service.addProperty("ALREADY_LAUNCHED", true);
                            service.publish();

                            this.launch(new ServiceLaunchBuilder(group.name()));
                        }
                    }
                };
            }

            if (max < online) {
                log.info("Shutting down {} services in group {} to maintain maximum of {}.", online - max, group.name(), max);
                this.services.stream()
                        .filter(it -> it.group().name().equals(group.name()))
                        .limit(online - max)
                        .map(it -> (ServiceImpl) it)
                        .forEach(ServiceImpl::shutdown);
            }
        }

    }

    @Override
    public Service get(String id) {
        var service = this.services.stream().filter(it -> it.id().equals(id)).findFirst().orElse(null);
        if (service == null) {
            log.error("Service {} not found.", id);
            return null;
        }
        return service;
    }

    @Override
    public void shutdown(Service service) {
        if (service == null) {
            log.error("Service is null.");
            return;
        }

        ((ServiceImpl) service).shutdown();
    }


    @Override
    public CompletableFuture<Service> launch(ServiceLaunchBuilder builder) {
        var defaultGroup = EasyCloudCluster.instance().groupProvider().get(builder.group());
        var group = new Group(defaultGroup.enabled(), defaultGroup.name(), defaultGroup.platform());
        builder.properties().forEach((key, value) -> group.properties().put(key, value));
        defaultGroup.properties().forEach((key, value) -> {
            if (!builder.properties().containsKey(key)) {
                group.properties().put(key, value);
            }
        });

        if (!group.enabled()) {
            log.error("Group {} is currently disabled.", group.name());
            return CompletableFuture.failedFuture(new IllegalArgumentException("Group " + group.name() + " is currently disabled."));
        }

        var port = this.freePort();
        if (group.platform().type().equals(PlatformType.PROXY)) {
            port = EasyCloudCluster.instance().configuration().local().proxyPort() + (int) this.services.stream().filter(it -> it.group().platform().type().equals(PlatformType.PROXY)).count();
        }
        if (port == -1) {
            log.error("No free port available.");
            return CompletableFuture.failedFuture(new RuntimeException("No free port available."));
        }


        var id = 0;
        for (int i = 0; i < 999; i++) {
            int finalId = id;
            if (this.services.stream().noneMatch(it -> it.id().equals(group.name() + "-" + finalId))) {
                break;
            }
            id++;
        }

        if (builder.id() > 0) {
            id = builder.id();

            if (this.services.stream().anyMatch(it -> it.id().equals(group.name() + "-" + builder.id()))) {
                log.error("Service with id {} already exists in group {}.", id, group.name());
                log.error("Custom launch ids are only allowed if the service is not already running.");
                return CompletableFuture.failedFuture(new IllegalArgumentException("Service with id " + id + " already exists in group " + group.name() + "."));
            }
        }

        var directory = Path.of("local").resolve(builder.property(GroupProperties.SAVE_FILES(), group.property(GroupProperties.SAVE_FILES())) ? "constant" : "dynamic").resolve(group.name() + "-" + id);
        var service = new ServiceImpl(group.name() + "-" + id, group, directory);
        service.addProperty(ServiceProperties.PORT(), port);
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), 0);

        var result = this.prepare(service);
        if (!result) {
            log.error("Failed to prepare service.");
            return CompletableFuture.failedFuture(new RuntimeException("Failed to prepare service."));
        }

        var process = ServiceLaunchFactory.create(service);
        service.process(process);

        log.info(EasyCloudCluster.instance().i18nProvider().get("service.launched", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset(), ansi().fgRgb(LogType.WHITE.rgb()).a(service.property(ServiceProperties.PORT())).reset()));

        this.services.add(service);
        EasyCloudCluster.instance().eventProvider().publish(new ServiceStartingEvent(builder.builderId(), service));
        return CompletableFuture.completedFuture(service);
    }

    @SneakyThrows
    private boolean prepare(Service service) {
        var resourcesPath = Path.of("resources");
        var templatePath = Path.of("local").resolve("templates");
        var group = service.group();

        //noinspection ResultOfMethodCallIgnored
        service.directory().toFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        service.directory().resolve("plugins").toFile().mkdirs();

        FileFactory.copy(templatePath.resolve("global").resolve("all"), service.directory());

        if (group.platform().type().equals(PlatformType.PROXY)) {
            FileFactory.copy(templatePath.resolve("global").resolve("proxy"), service.directory());
            FileFactory.copy(templatePath.resolve("proxy").resolve(service.group().name()), service.directory());

            var secretPath = service.directory().resolve("forwarding.secret");
            if (Files.exists(secretPath)) {
                Files.delete(secretPath);
            }
            Files.write(secretPath, EasyCloudCluster.instance().configuration().security().value().getBytes());
        } else {
            FileFactory.copy(templatePath.resolve("global").resolve("server"), service.directory());
            FileFactory.copy(templatePath.resolve("server").resolve(service.group().name()), service.directory());
        }

        EasyCloudCluster.instance().platformProvider().initializer(group.platform().initializerId()).initialize(service.directory());
        FileFactory.write(service.directory(), new ServiceDataConfiguration(service.id(), EasyCloudCluster.instance().configuration().security().value(), EasyCloudCluster.instance().configuration().local().clusterPort()));

        try {
            Files.copy(resourcesPath.resolve("easycloud-service.jar"), service.directory().resolve("easycloud-service.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy server plugin. ({})", service.id(), exception);
            return false;
        }

        EasyCloudCluster.instance().moduleService().modules()
                .entrySet()
                .stream()
                .filter(it -> Arrays.stream(it.getKey().platforms()).toList().stream().anyMatch(it2 -> it2.equals(service.group().platform().initializerId())))
                .map(Map.Entry::getValue)
                .forEach(path -> {
                    try {
                        Files.copy(path, service.directory().resolve("plugins").resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        if (!service.directory().resolve("platform.jar").toFile().exists()) {
            try {
                Files.copy(resourcesPath.resolve("groups").resolve("platforms").resolve(group.platform().initializerId() + "-" + group.platform().version() + ".jar"), service.directory().resolve("platform.jar"));
            } catch (Exception exception) {
                log.error("Failed to copy platform jar.", exception);
                return false;
            }
        }
        return true;
    }

    private int freePort() {
        var port = 4000;
        while (port < 5000) {
            int finalPort = port;
            if (this.services.stream().noneMatch(it -> it.property(ServiceProperties.PORT()) == finalPort)) {
                return port;
            }
            port++;
        }
        return -1;
    }
}
