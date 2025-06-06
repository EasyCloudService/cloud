package dev.easycloud.service.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Platform {
    private final String initializerId;

    private final String version;
    private final PlatformType type;
}
