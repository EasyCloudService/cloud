package dev.easycloud.service.command;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Getter
@Accessors(fluent = true)
public abstract class Command {
    private final String name;
    private final String description;

    private final List<CommandNode> commandNodes = new ArrayList<>();

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void executeBase() {
        log.info("Command not implemented!");
    }

    public void addSubCommand(CommandNode commandNode) {
        this.commandNodes.add(commandNode);
    }
}
