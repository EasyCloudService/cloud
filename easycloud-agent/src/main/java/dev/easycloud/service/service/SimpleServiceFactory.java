package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.scheduler.EasyScheduler;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.easycloud.service.service.resources.ServiceLaunchBuilder;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class SimpleServiceFactory implements ServiceFactory {
    @Getter
    private final List<Service> services = new ArrayList<>();

    public SimpleServiceFactory() {
        new EasyScheduler(this::refresh).repeat(TimeUnit.SECONDS.toMillis(1));
    }

    public void refresh() {
        for (Group group : EasyCloudAgent.instance().groupFactory().groups()) {
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
    public void launch(Group group) {
        this.launch(group, 1);
    }

    @Override
    public void launch(Group group, int count) {
        var port = this.freePort();
        if(group.platform().type().equals(PlatformType.PROXY)) {
            port = 25565;
        }
        if (port == -1) {
            log.error("No free port available.");
            return;
        }
        var id = this.services.stream().filter(it -> it.group().name().equals(group.name())).count() + 1;

        var storagePath = Path.of("storage");
        var directory = Path.of(group.data().isStatic() ? "static" : "services").resolve(group.name() + "-" + id);
        var service = new SimpleService(group.name() + "-" + id, group, port, directory);

        service.directory().toFile().mkdirs();
        service.directory().resolve("plugins").toFile().mkdirs();

        FileFactory.write(service.directory(), new ServiceDataConfiguration(service.id(), EasyCloudAgent.instance().privateKey()));

        try {
            Files.copy(storagePath.resolve("jars").resolve("easycloud-plugin.jar"), service.directory().resolve("plugins").resolve("easycloud-plugin.jar"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error("Failed to copy server plugin. ({})", service.id(), exception);
            return;
        }

        if(!service.directory().resolve("platform.jar").toFile().exists()) {
            try {
                Files.copy(storagePath.resolve("platforms").resolve(group.platform().initilizerId() + "-" + group.platform().version() + ".jar"), service.directory().resolve("platform.jar"));
            } catch (Exception exception) {
                log.error("Failed to copy platform jar.", exception);
                return;
            }
        }

        var process = ServiceLaunchBuilder.create(service);
        service.process(process);

        log.info("Service {} launched on port {}.", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset(), ansi().fgRgb(LogType.WHITE.rgb()).a(service.port()).reset());

        this.services.add(service);
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
