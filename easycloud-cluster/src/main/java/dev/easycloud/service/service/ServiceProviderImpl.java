package dev.easycloud.service.service;

import com.google.inject.Inject;
import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.configuration.ClusterConfiguration;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.group.GroupProvider;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.module.ModuleService;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceStartingEvent;
import dev.easycloud.service.platform.PlatformProvider;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.scheduler.EasyScheduler;
import dev.easycloud.service.service.builder.ServiceLaunchFactory;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import dev.easycloud.service.service.listener.*;
import dev.easycloud.service.service.resources.*;
import dev.easycloud.service.terminal.Terminal;
import dev.easycloud.service.terminal.TerminalState;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

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

    private final Terminal terminal;
    private final ClusterConfiguration configuration;
    private final I18nProvider i18nProvider;
    private final ModuleService moduleService;
    private final PlatformProvider platformProvider;
    private final EventProvider eventProvider;
    private final GroupProvider groupProvider;

    @Inject
    public ServiceProviderImpl(
            Terminal terminal, ClusterConfiguration configuration, I18nProvider I18nProvider,
            PlatformProvider platformProvider, ModuleService moduleService,
            EventProvider eventProvider, GroupProvider groupProvider
    ) {
        this.terminal = terminal;
        this.configuration = configuration;
        this.i18nProvider = I18nProvider;
        this.moduleService = moduleService;
        this.platformProvider = platformProvider;
        this.eventProvider = eventProvider;
        this.groupProvider = groupProvider;

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
        if(this.terminal.state().equals(TerminalState.STOPPING)) {
            return;
        }

        for (ServiceImpl service : this.services.stream().map(it -> (ServiceImpl) it).filter(it -> it.process() == null || !it.process().isAlive()).toList()) {
            this.shutdown(service);
        }

        if (this.groupProvider == null) {
            return;
        }

        var currentStarting = this.services.stream().filter(it -> it.state().equals(ServiceState.STARTING)).count();
        if (currentStarting >= this.configuration.local.getStartingSameTime()) {
            return;
        }

        for (Group group : this.groupProvider.groups()
                .stream()
                .sorted((o1, o2) -> {
                    if (o1.read(GroupProperties.PRIORITY()) == null) {
                        log.error("Group {} has no priority set, defaulting to 0.", o1.getName());
                        return 0;
                    }
                    if (o2.read(GroupProperties.PRIORITY()) == null) {
                        log.error("Group {} has no priority set, defaulting to 0.", o2.getName());
                        return 0;
                    }
                    return Integer.compare(o2.read(GroupProperties.PRIORITY()), o1.read(GroupProperties.PRIORITY()));
                })
                .filter(Group::getEnabled)
                .toList()) {
            var always = group.read(GroupProperties.ALWAYS_RUNNING());
            var max = group.read(GroupProperties.MAXIMUM_RUNNING());
            if (max == -1) {
                max = Integer.MAX_VALUE;
            }
            var online = this.services.stream().filter(it -> it.group().getName().equals(group.getName())).count();
            if (always > online && online < max) {
                for (int i = 0; i < always - online; i++) {
                    if (this.services.stream().filter(it -> it.group().getName().equals(group.getName())).count() >= max) {
                        break;
                    }
                    if (currentStarting >= this.configuration.local.getStartingSameTime()) {
                        return;
                    }
                    this.launch(new ServiceLaunchBuilder(group.getName()));
                    currentStarting++;
                }
            }

            online = this.services.stream().filter(it -> it.group().getName().equals(group.getName())).count();
            var percentage = this.configuration.local.getDynamicPercentage() / 100.0;

            if (online < max) {
                for (Service service : this.services().stream().filter(it -> it.group().getName().equals(group.getName())).toList()) {
                    if (service.property("ALREADY_LAUNCHED", Boolean.class) != null && service.property("ALREADY_LAUNCHED", Boolean.class)) {
                        continue;
                    }

                    if (service.state().equals(ServiceState.ONLINE)) {
                        if (((double) service.property(ServiceProperties.ONLINE_PLAYERS()) / group.read(GroupProperties.MAX_PLAYERS())) >= percentage) {
                            service.addProperty("ALREADY_LAUNCHED", true);
                            service.publish();

                            this.launch(new ServiceLaunchBuilder(group.getName()));
                        }
                    }
                }
                ;
            }

            if (max < online) {
                log.info("Shutting down {} services in group {} to maintain maximum of {}.", online - max, group.getName(), max);
                this.services.stream()
                        .filter(it -> it.group().getName().equals(group.getName()))
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
        var defaultGroup = this.groupProvider.get(builder.group());
        var group = new Group(defaultGroup.getEnabled(), defaultGroup.getName(), defaultGroup.getPlatform());
        builder.properties().forEach((key, value) -> group.getProperties().put(key, value));
        defaultGroup.getProperties().forEach((key, value) -> {
            if (!builder.properties().containsKey(key)) {
                group.getProperties().put(key, value);
            }
        });

        if (!group.getEnabled()) {
            log.error("Group {} is currently disabled.", group.getName());
            return CompletableFuture.failedFuture(new IllegalArgumentException("Group " + group.getName() + " is currently disabled."));
        }

        var port = this.freePort();
        if (group.getPlatform().type().equals(PlatformType.PROXY)) {
            port = this.configuration.local.getProxyPort() + (int) this.services.stream().filter(it -> it.group().getPlatform().type().equals(PlatformType.PROXY)).count();
        }
        if (port == -1) {
            log.error("No free port available.");
            return CompletableFuture.failedFuture(new RuntimeException("No free port available."));
        }


        var id = 0;
        for (int i = 0; i < 999; i++) {
            int finalId = id;
            if (this.services.stream().noneMatch(it -> it.id().equals(group.getName() + "-" + finalId))) {
                break;
            }
            id++;
        }

        if (builder.id() > 0) {
            id = builder.id();

            if (this.services.stream().anyMatch(it -> it.id().equals(group.getName() + "-" + builder.id()))) {
                log.error("Service with id {} already exists in group {}.", id, group.getName());
                log.error("Custom launch ids are only allowed if the service is not already running.");
                return CompletableFuture.failedFuture(new IllegalArgumentException("Service with id " + id + " already exists in group " + group.getName() + "."));
            }
        }

        var directory = Path.of("local").resolve(builder.property(GroupProperties.SAVE_FILES(), group.read(GroupProperties.SAVE_FILES())) ? "constant" : "dynamic").resolve(group.getName() + "-" + id);
        var service = new ServiceImpl(group.getName() + "-" + id, group, directory);
        service.addProperty(ServiceProperties.PORT(), port);
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), 0);

        var result = this.prepare(service);
        if (!result) {
            log.error("Failed to prepare service.");
            return CompletableFuture.failedFuture(new RuntimeException("Failed to prepare service."));
        }

        var process = ServiceLaunchFactory.create(service);
        service.process(process);

        log.info(this.i18nProvider.get("service.launched", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(service.id()).reset(), ansi().fgRgb(Log4jColor.WHITE.rgb()).a(service.property(ServiceProperties.PORT())).reset()));

        this.services.add(service);
        this.eventProvider.publish(new ServiceStartingEvent(builder.builderId(), service));
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

        FileUtils.copyDirectory(templatePath.resolve("global").resolve("all").toFile(), service.directory().toFile());

        if (group.getPlatform().type().equals(PlatformType.PROXY)) {
            FileUtils.copyDirectory(templatePath.resolve("global").resolve("proxy").toFile(), service.directory().toFile());
            FileUtils.copyDirectory(templatePath.resolve("proxy").resolve(service.group().getName()).toFile(), service.directory().toFile());

            var secretPath = service.directory().resolve("forwarding.secret");
            if (Files.exists(secretPath)) {
                Files.delete(secretPath);
            }
            Files.write(secretPath, this.configuration.security.getValue().getBytes());
        } else {
            FileUtils.copyDirectory(templatePath.resolve("global").resolve("server").toFile(), service.directory().toFile());
            FileUtils.copyDirectory(templatePath.resolve("server").resolve(service.group().getName()).toFile(), service.directory().toFile());
        }

        this.platformProvider.initializer(group.getPlatform().initializerId()).initialize(service.directory());
        Configurations.Companion.write(service.directory(), new ServiceDataConfiguration(service.id(), this.configuration.security.getValue(), this.configuration.local.getClusterPort()));

        try {
            Files.copy(resourcesPath.resolve("easycloud-service.jar"), service.directory().resolve("easycloud-service.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy server plugin. ({})", service.id(), exception);
            return false;
        }

        this.moduleService.modules()
                .entrySet()
                .stream()
                .filter(it -> Arrays.stream(it.getKey().platforms()).toList().stream().anyMatch(it2 -> it2.equals(service.group().getPlatform().initializerId())))
                .map(Map.Entry::getValue)
                .forEach(path -> {
                    try {
                        Files.copy(path, service.directory().resolve("plugins").resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        try {
            Files.copy(resourcesPath.resolve("groups").resolve("platforms").resolve(group.getPlatform().initializerId() + "-" + group.getPlatform().version() + ".jar"), service.directory().resolve("platform.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy platform jar.", exception);
            return false;
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
