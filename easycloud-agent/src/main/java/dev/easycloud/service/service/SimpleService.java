package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor
@Setter
public final class SimpleService implements Service {
    private final String id;
    private final Group group;

    private final int port;
    private final Path directory;

    private Process process;

    @Override
    public void shutdown() {
        // TODO
    }
}
