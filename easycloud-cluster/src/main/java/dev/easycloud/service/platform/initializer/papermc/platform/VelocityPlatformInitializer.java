package dev.easycloud.service.platform.initializer.papermc.platform;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.platform.initializer.papermc.AbstractPaperMCInitializer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public final class VelocityPlatformInitializer extends AbstractPaperMCInitializer {
    private final PlatformType type = PlatformType.PROXY;

    public VelocityPlatformInitializer() {
        super("velocity", "https://api.papermc.io/v2/projects/velocity");
    }

    @Override
    @SneakyThrows
    public void initialize(Path path) {
        if(!Files.exists(path.resolve("velocity.toml"))) {
            Files.copy(Objects.requireNonNull(EasyCloudClusterOld.class.getClassLoader().getResourceAsStream("platform/velocity/velocity.toml")), path.resolve("velocity.toml"));
        }
    }

    @Override
    public List<Platform> platforms() {
        List<Platform> tmp = new ArrayList<>();
        var versions = this.versions();
        if(versions == null) {
            log.error(EasyCloudClusterOld.instance().i18nProvider().get("group.platform.fetch.failed", "Velocity"));
            return new ArrayList<>();
        }

        versions.forEach(version -> tmp.add(new Platform(this.id, version, this.type)));
        return tmp;
    }
}
