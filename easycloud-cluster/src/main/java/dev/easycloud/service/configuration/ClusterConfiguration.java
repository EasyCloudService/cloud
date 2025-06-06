package dev.easycloud.service.configuration;

import dev.easycloud.service.file.FileFactory;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public final class ClusterConfiguration {
    private final LocalConfiguration local;
    private final SecurityConfiguration security;

    public ClusterConfiguration() {
        var path = Path.of("resources").resolve("config");
        path.toFile().mkdirs();

        FileFactory.writeIfNotExists(path, new LocalConfiguration());
        this.local = FileFactory.read(path, LocalConfiguration.class);

        FileFactory.writeIfNotExists(path, new SecurityConfiguration());
        this.security = FileFactory.read(path, SecurityConfiguration.class);
    }
}
