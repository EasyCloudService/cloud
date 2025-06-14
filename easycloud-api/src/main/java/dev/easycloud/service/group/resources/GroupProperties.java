package dev.easycloud.service.group.resources;

import dev.easycloud.service.property.Property;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class GroupProperties {
    @Getter
    private final Property<Integer> MEMORY = new Property<>("MEMORY", Integer.class);
    @Getter
    private final Property<Integer> MAX_PLAYERS = new Property<>("MAX_PLAYERS", Integer.class);
    @Getter
    private final Property<Integer> ALWAYS_RUNNING = new Property<>("ALWAYS_RUNNING", Integer.class);
    @Getter
    private final Property<Integer> MAXIMUM_RUNNING = new Property<>("MAXIMUM_RUNNING", Integer.class);
    @Getter
    private final Property<Boolean> SAVE_FILES = new Property<>("SAVE_FILES", Boolean.class);
    @Getter
    private final Property<Integer> PRIORITY = new Property<>("PRIORITY", Integer.class);
}
