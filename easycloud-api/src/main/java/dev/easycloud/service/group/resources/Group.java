package dev.easycloud.service.group.resources;

import dev.easycloud.service.platform.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Group {
    private final String name;
    private final Platform platform;

    private final GroupData data;
}
