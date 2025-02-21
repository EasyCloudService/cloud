package dev.easycloud.service.platform;

import dev.easycloud.service.platform.types.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class PlatformFactory {
    @Getter
    private final List<Platform> platforms = new ArrayList<>();
    private final List<PlatformInitializer> initializers = new ArrayList<>();

    public PlatformFactory() {
        this.initializers.add(new PaperPlatformInitializer());
        this.initializers.add(new VelocityPlatformInitializer());
    }

    public void refresh() {
        this.platforms.clear();
        for (PlatformInitializer initializer : this.initializers) {
            this.platforms.addAll(initializer.platforms());
        }
    }
}
