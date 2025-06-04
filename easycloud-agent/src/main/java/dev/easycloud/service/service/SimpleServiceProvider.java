package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.scheduler.EasyScheduler;
import dev.easycloud.service.service.listener.ServiceReadyListener;
import dev.easycloud.service.service.listener.ServiceShutdownListener;
import dev.easycloud.service.service.resources.*;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class SimpleServiceProvider implements ServiceProvider {
    @Getter
    private final List<Service> services = new ArrayList<>();

    public SimpleServiceProvider() {
        new EasyScheduler(this::refresh).repeat(TimeUnit.SECONDS.toMillis(1));

        var templatePath = Path.of("local").resolve("templates");
        templatePath.resolve("global").resolve("all").toFile().mkdirs();
        templatePath.resolve("global").resolve("server").toFile().mkdirs();
        templatePath.resolve("global").resolve("proxy").toFile().mkdirs();

        new ServiceReadyListener();
        new ServiceShutdownListener();

        EasyCloudAgent.instance().netServer().track(RequestServiceLaunchPacket.class, packet -> {
            this.launch(EasyCloudAgent.instance().groupProvider().get(packet.groupName()), packet.amount());
        });
        EasyCloudAgent.instance().netServer().track(RequestServiceInformationPacket.class, (channel, packet) -> {
            EasyCloudAgent.instance().eventProvider().publish(new ServiceReadyEvent(this.get(packet.serviceId())));

            log.info("request service info: {}", packet.serviceId());
            //channel.send(new ServiceInformationPacket(this.get(packet.serviceId()), this.services.stream().filter(it -> !it.id().equals(packet.serviceId())).toList()));
        });
    }

    public void refresh() {
        for (SimpleService service : this.services.stream().map(it -> (SimpleService) it).filter(it -> it.process() == null || !it.process().isAlive()).toList()) {
            this.shutdown(service);
        }

        for (Group group : EasyCloudAgent.instance().groupProvider().groups().stream().filter(Group::enabled).toList()) {
            var always = group.properties().always();
            var max = group.properties().maximum();
            var online = this.services.stream().filter(it -> it.group().name().equals(group.name())).count();

            if(always > online) {
                this.launch(group, (int) (always - online));
            }
            if(max < online && max != -1) {
                this.services.stream()
                        .filter(it -> it.group().name().equals(group.name()))
                        .limit(online - max)
                        .map(it -> (SimpleService) it)
                        .forEach(SimpleService::shutdown);
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

        ((SimpleService) service).shutdown();
    }

    @Override
    public void launch(Group group) {
        if(!group.enabled()) {
            log.error("Group {} is currently disabled.", group.name());
            return;
        }

        var port = this.freePort();
        if(group.platform().type().equals(PlatformType.PROXY)) {
            port = 25565;
        }
        if (port == -1) {
            log.error("No free port available.");
            return;
        }
        var id = this.services.stream().filter(it -> it.group().name().equals(group.name())).count() + 1;

        var directory = Path.of("local").resolve(group.properties().saveFiles() ? "static" : "services").resolve(group.name() + "-" + id);
        var service = new SimpleService(group.name() + "-" + id, group, port, directory);

        var result = this.prepare(service);
        if (!result) {
            log.error("Failed to prepare service.");
            return;
        }

        var process = ServiceLaunchBuilder.create(service);
        service.process(process);

        log.info(EasyCloudAgent.instance().i18nProvider().get("service.launched", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset(), ansi().fgRgb(LogType.WHITE.rgb()).a(service.port()).reset()));

        this.services.add(service);
    }

    @SneakyThrows
    private boolean prepare(Service service) {
        var resourcesPath = Path.of("resources");
        var templatePath = Path.of("local").resolve("templates");
        var group = service.group();

        service.directory().toFile().mkdirs();
        service.directory().resolve("plugins").toFile().mkdirs();

        FileFactory.copy(templatePath.resolve("global").resolve("all"), service.directory());

        if(group.platform().type().equals(PlatformType.PROXY)) {
            FileFactory.copy(templatePath.resolve("global").resolve("proxy"), service.directory());
            FileFactory.copy(templatePath.resolve("proxy").resolve(service.group().name()), service.directory());

            var secretPath = service.directory().resolve("forwarding.secret");
            if(Files.exists(secretPath)) {
                Files.delete(secretPath);
            }
            Files.write(secretPath, EasyCloudAgent.instance().configuration().key().getBytes());
        } else {
            FileFactory.copy(templatePath.resolve("global").resolve("server"), service.directory());
            FileFactory.copy(templatePath.resolve("server").resolve(service.group().name()), service.directory());
        }

        EasyCloudAgent.instance().platformProvider().initializer(group.platform().initializerId()).initialize(service.directory());
        FileFactory.write(service.directory(), new ServiceDataConfiguration(service.id(), EasyCloudAgent.instance().configuration().key()));

        try {
            Files.copy(resourcesPath.resolve("easycloud-plugin.jar"), service.directory().resolve("plugins").resolve("easycloud-plugin.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy server plugin. ({})", service.id(), exception);
            return false;
        }

        if(!service.directory().resolve("platform.jar").toFile().exists()) {
            try {
                Files.copy(resourcesPath.resolve("platforms").resolve(group.platform().initializerId() + "-" + group.platform().version() + ".jar"), service.directory().resolve("platform.jar"));
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
            if (this.services.stream().noneMatch(it -> it.port() == finalPort)) {
                return port;
            }
            port++;
        }
        return -1;
    }
}
