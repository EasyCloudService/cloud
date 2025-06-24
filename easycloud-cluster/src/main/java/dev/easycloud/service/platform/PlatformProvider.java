package dev.easycloud.service.platform;

import dev.easycloud.service.platform.initializer.papermc.platform.PaperPlatformInitializer;
import dev.easycloud.service.platform.initializer.papermc.platform.VelocityPlatformInitializer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class PlatformProvider {
    private final List<Platform> platforms = new ArrayList<>();
    private final List<PlatformInitializer> initializers = new ArrayList<>();

    public PlatformProvider() {
        this.initializers.add(new PaperPlatformInitializer());
        this.initializers.add(new VelocityPlatformInitializer());
    }

    public PlatformInitializer initializer(String id) {
        return this.initializers.stream().filter(it -> it.id().equals(id)).findFirst().orElse(null);
    }

    public void search() {
        this.platforms.clear();
        for (PlatformInitializer initializer : this.initializers) {
            this.platforms.addAll(initializer.platforms());
        }
    }

    public List<Platform> platforms() {
        return this.platforms;
    }

    public List<PlatformInitializer> initializers() {
        return this.initializers;
    }

}
