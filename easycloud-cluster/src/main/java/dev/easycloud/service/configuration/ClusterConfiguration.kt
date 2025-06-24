package dev.easycloud.service.configuration;

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

        Configurations.Companion.writeIfNotExists(path, new LocalConfiguration());
        Configurations.Companion.writeIfNotExists(path, new SecurityConfiguration());

        this.reload();
    }

    public void publish(Object object) {
        if (object instanceof LocalConfiguration localConfig) {
            this.local = localConfig;
            Configurations.Companion.write(path, localConfig);
        } else if (object instanceof SecurityConfiguration securityConfig) {
            this.security = securityConfig;
            Configurations.Companion.write(path, securityConfig);
        } else {
            throw new IllegalArgumentException("Unsupported configuration type: " + object.getClass().getName());
        }
    }

    public void reload() {
        this.local = Configurations.Companion.read(path, LocalConfiguration.class);
        this.security = Configurations.Companion.read(path, SecurityConfiguration.class);
    }
}
