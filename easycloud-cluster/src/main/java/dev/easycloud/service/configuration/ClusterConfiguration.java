package dev.easycloud.service.configuration;

import dev.easycloud.service.file.FileFactory;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public final class ClusterConfiguration {
    private final Path path = Path.of("resources").resolve("config");

    private LocalConfiguration local;
    private SecurityConfiguration security;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ClusterConfiguration() {
        var path = Path.of("resources").resolve("config");
        path.toFile().mkdirs();

        FileFactory.writeIfNotExists(path, new LocalConfiguration());
        FileFactory.writeIfNotExists(path, new SecurityConfiguration());

        this.reload();
    }

    public void reload() {
        this.local = FileFactory.read(path, LocalConfiguration.class);
        this.security = FileFactory.read(path, SecurityConfiguration.class);
    }
}
