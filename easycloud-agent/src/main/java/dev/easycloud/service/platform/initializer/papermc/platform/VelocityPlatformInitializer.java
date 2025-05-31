package dev.easycloud.service.platform.initializer.papermc.platform;

import dev.easycloud.service.EasyCloudAgent;
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

@Slf4j
@Getter
public class VelocityPlatformInitializer extends AbstractPaperMCInitializer {
    private final PlatformType type = PlatformType.PROXY;

    public VelocityPlatformInitializer() {
        super("velocity");
    }

    @Override
    @SneakyThrows
    public void initialize(Path path) {
        if(!Files.exists(path.resolve("velocity.toml"))) {
            Files.copy(EasyCloudAgent.class.getClassLoader().getResourceAsStream("platform/velocity/velocity.toml"), path.resolve("velocity.toml"));
        }
    }

    @Override
    public List<Platform> platforms() {
        List<Platform> tmp = new ArrayList<>();
        var versions = this.versions();
        if(versions == null) {
            log.error(EasyCloudAgent.instance().i18nProvider().get("group.platform.fetch.failed", "Velocity"));
            return new ArrayList<>();
        }

        versions.forEach(version -> {
            tmp.add(new Platform(this.id, version, this.type));
        });
        return tmp;
    }
}
