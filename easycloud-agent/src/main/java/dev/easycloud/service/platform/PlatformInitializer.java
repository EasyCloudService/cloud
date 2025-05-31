package dev.easycloud.service.platform;

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
