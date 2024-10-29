package dev.easycloud.service.command;

import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Accessors(fluent = true)
public abstract class Command {
    private final String name;
    private final String description;
    private final List<String> aliases;

    private final Map<String, Consumer<String[]>> subCommands = new HashMap<>();

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = Arrays.stream(aliases).toList();
    }

    public void executeBase() {
        SimpleLogger.info("Command not implemented!");
    }

    public void addSubCommand(String name, Consumer<String[]> onExecute) {
        this.subCommands.put(name, onExecute);
    }
}
