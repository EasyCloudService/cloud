package dev.easycloud.service.command;

import dev.easycloud.service.command.resources.CategoryCommand;
import dev.easycloud.service.command.resources.ClearCommand;
import dev.easycloud.service.command.resources.HelpCommand;
import dev.easycloud.service.command.resources.ShutdownCommand;
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
        this.commands.addAll(List.of(new HelpCommand(), new ClearCommand(), new ShutdownCommand(), new CategoryCommand()));
    }

    public void execute(String command, String[] args) {
        this.commands.stream()
                .filter(it -> it.name().equals(command) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(command)))
                .findFirst()
                .ifPresentOrElse(it -> {
                    if (args.length == 0 || !it.subCommands().containsKey(args[1])) {
                        it.executeBase();
                    } else {
                        it.subCommands().get(args[1]).accept(args);
                    }
                }, () -> SimpleLogger.info("This command does not exist!"));
    }
}
