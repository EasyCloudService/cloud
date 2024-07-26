package dev.easycloud.service.command;

import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class Command {
    private final String name;
    private final String description;
    private final List<String> alias;

    public void execute(String[] args) {
        SimpleLogger.info("Command not implemented!");
    }
}
