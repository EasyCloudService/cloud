package dev.easycloud.service.platform.types;

import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;

import java.nio.file.Path;
import java.util.List;

public interface PlatformInitializer {
    String id();
    String url();
    PlatformType type();

    void initialize(Path path);
    String buildDownload(String version);
    List<Platform> platforms();
}
