package dev.easycloud.service.group.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class GroupProperties {
    private int memory;

    private int maxPlayers;

    private int always;
    private int maximum;

    private boolean saveFiles;
}
