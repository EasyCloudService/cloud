package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.ServiceShutdownPacket;
import dev.easycloud.service.network.packet.proxy.RegisterServerPacket;
import dev.easycloud.service.network.packet.proxy.UnregisterServerPacket;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.scheduler.EasyScheduler;
import dev.easycloud.service.service.resources.*;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class SimpleServiceHandler implements ServiceHandler {
    @Getter
    private final List<Service> services = new ArrayList<>();

    public SimpleServiceHandler() {
        new EasyScheduler(this::refresh).repeat(TimeUnit.SECONDS.toMillis(1));

        var templatePath = Path.of("template");
        templatePath.resolve("global").resolve("all").toFile().mkdirs();
        templatePath.resolve("global").resolve("server").toFile().mkdirs();
        templatePath.resolve("global").resolve("proxy").toFile().mkdirs();

        EasyCloudAgent.instance().netServer().track(ServiceReadyPacket.class, (client, packet) -> {
            var service = get(packet.serviceId());
            if (service == null) {
                return;
            }

            service.state(ServiceState.ONLINE);
            if(service.group().platform().type().equals(PlatformType.SERVER)) {
                EasyCloudAgent.instance().netServer().broadcast(new RegisterServerPacket(service.id(), new InetSocketAddress(service.port())));
            }
            if(service.group().platform().type().equals(PlatformType.PROXY)) {
                this.services.forEach(it -> client.send(new RegisterServerPacket(it.id(), new InetSocketAddress(it.port()))));
            }
            log.info("Service {} is now ready.", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset());
        });

        EasyCloudAgent.instance().netServer().track(ServiceShutdownPacket.class, packet -> {
            var service = get(packet.serviceId());
            if (service == null) {
                return;
            }

            if(service.group().platform().type().equals(PlatformType.SERVER)) {
                EasyCloudAgent.instance().netServer().broadcast(new UnregisterServerPacket(service.id()));
            }
            this.shutdown(service);
        });
    }

    public void refresh() {
        for (SimpleService service : this.services.stream().map(it -> (SimpleService) it).filter(it -> it.process() == null || !it.process().isAlive()).toList()) {
            this.shutdown(service);
        }

        for (Group group : EasyCloudAgent.instance().groupHandler().groups().stream().filter(Group::enabled).toList()) {
            var always = group.data().always();
            var max = group.data().maximum();
            var online = this.services.stream().filter(it -> it.group().name().equals(group.name())).count();

            if(always > online) {
                this.launch(group, (int) (always - online));
            }
            if(max < online && max != -1) {
                this.services.stream()
                        .filter(it -> it.group().name().equals(group.name()))
                        .limit(online - max)
                        .forEach(Service::shutdown);
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

        service.shutdown();
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

        var directory = Path.of(group.data().isStatic() ? "static" : "services").resolve(group.name() + "-" + id);
        var service = new SimpleService(group.name() + "-" + id, group, port, directory);

        var result = this.prepare(service);
        if (!result) {
            log.error("Failed to prepare service.");
            return;
        }

        var process = ServiceLaunchBuilder.create(service);
        service.process(process);

        log.info("Service {} launched on port {}.", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset(), ansi().fgRgb(LogType.WHITE.rgb()).a(service.port()).reset());

        this.services.add(service);
    }

    @Override
    public void launch(Group group, int count) {
        for (int i = 0; i < count; i++) {
            this.launch(group);
        }
    }

    @SneakyThrows
    private boolean prepare(Service service) {
        var storagePath = Path.of("storage");
        var templatePath = Path.of("template");
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
            Files.write(secretPath, EasyCloudAgent.instance().securityKey().getBytes());
        } else {
            FileFactory.copy(templatePath.resolve("global").resolve("server"), service.directory());
            FileFactory.copy(templatePath.resolve("server").resolve(service.group().name()), service.directory());
        }

        EasyCloudAgent.instance().platformHandler().initializer(group.platform().initilizerId()).initialize(service.directory());
        FileFactory.write(service.directory(), new ServiceDataConfiguration(service.id(), EasyCloudAgent.instance().securityKey()));

        try {
            Files.copy(storagePath.resolve("easycloud-plugin.jar"), service.directory().resolve("plugins").resolve("easycloud-plugin.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy server plugin. ({})", service.id(), exception);
            return false;
        }

        if(!service.directory().resolve("platform.jar").toFile().exists()) {
            try {
                Files.copy(storagePath.resolve("platforms").resolve(group.platform().initilizerId() + "-" + group.platform().version() + ".jar"), service.directory().resolve("platform.jar"));
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
