package dev.easycloud.service.group.resources;

import dev.easycloud.service.property.Property;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class GroupProperties {
    @Getter
    private final Property<Integer> MEMORY = new Property<>("memory", Integer.class);
    @Getter
    private final Property<Integer> MAX_PLAYERS = new Property<>("max_players", Integer.class);
    @Getter
    private final Property<Integer> ALWAYS_RUNNING = new Property<>("always_running", Integer.class);
    @Getter
    private final Property<Integer> MAXIMUM_RUNNING = new Property<>("maximum_running", Integer.class);
    @Getter
    private final Property<Boolean> SAVE_FILES = new Property<>("save_files", Boolean.class);
    @Getter
    private final Property<Integer> PRIORITY = new Property<>("priority", Integer.class);
    @Getter
    private final Property<Boolean> DYNAMIC_SIZE = new Property<>("dynamic_size", Boolean.class);
}
