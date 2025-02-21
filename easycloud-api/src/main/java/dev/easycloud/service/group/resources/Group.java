package dev.easycloud.service.group.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Group {
    private final String name;
    private final GroupType type;

    private final GroupData data;
}
