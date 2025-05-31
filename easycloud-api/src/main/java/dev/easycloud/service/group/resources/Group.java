package dev.easycloud.service.group.resources;

import dev.easycloud.service.platform.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class Group {
    private boolean enabled;

    private final String name;
    private final Platform platform;

    private final GroupProperties properties;
}