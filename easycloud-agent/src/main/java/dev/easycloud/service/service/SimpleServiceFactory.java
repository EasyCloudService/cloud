package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.service.resources.ServiceLaunchBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class SimpleServiceFactory implements ServiceFactory {
    @Getter
    private final List<Service> services = new ArrayList<>();

    @Override
    public void launch(Group group) {
        this.launch(group, 1);
    }

    @Override
    @SneakyThrows
    public void launch(Group group, int count) {
        var port = this.freePort();
        if (port == -1) {
            log.error("No free port available.");
            return;
        }
        var id = this.services.stream().filter(it -> it.group().name().equals(group.name())).count() + 1;
        var service = new SimpleService(group.name() + "-" + id, group, port, Path.of("services").resolve(group.name() + "-" + id), null);
        service.directory().toFile().mkdirs();
        Files.copy(Path.of("storage").resolve("platforms").resolve(group.platform().id() + ".jar"), service.directory().resolve("platform.jar"), StandardCopyOption.REPLACE_EXISTING);

        var process = ServiceLaunchBuilder.create(service);
        service.process(process);

        EasyCloudAgent.instance().processList().add(process);

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
