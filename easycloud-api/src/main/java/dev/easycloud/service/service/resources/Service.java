package dev.easycloud.service.service.resources;

import dev.easycloud.service.group.resources.Group;

import java.nio.file.Path;

public interface Service {
    String id();
    Group group();

    ServiceState state();
    void state(ServiceState state);

    int port();
    Path directory();

    void execute(String command);
    void shutdown();
}
