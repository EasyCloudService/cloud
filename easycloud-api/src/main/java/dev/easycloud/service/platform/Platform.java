package dev.easycloud.service.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Platform {
    private final String id;
    private final String initilizerId;

    private final String version;
    private final PlatformType type;
}
