package dev.easycloud.service.command;

import dev.easycloud.service.command.impl.ClearCommand;
import dev.easycloud.service.command.impl.HelpCommand;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class CommandHandler {
    private final List<Command> commands;

    public CommandHandler() {
        this.commands = new ArrayList<>();
        this.commands.addAll(List.of(new HelpCommand(), new ClearCommand()));
    }

    public void execute(String command, String[] args) {
        this.commands.stream()
                .filter(it -> it.name().equals(command) || it.alias().stream().anyMatch(it2 -> it2.equalsIgnoreCase(command)))
                .findFirst()
                .ifPresentOrElse(it -> it.execute(args), () -> SimpleLogger.info("This command does not exist!"));
    }
}
