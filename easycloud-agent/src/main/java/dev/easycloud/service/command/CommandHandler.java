package dev.easycloud.service.command;

import dev.easycloud.service.command.resources.CategoryCommand;
import dev.easycloud.service.command.resources.ClearCommand;
import dev.easycloud.service.command.resources.HelpCommand;
import dev.easycloud.service.command.resources.ShutdownCommand;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
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
                    if (args.length == 0 || it.subCommands().stream().noneMatch(it2 -> it2.name().equalsIgnoreCase(args[0]))) {
                        it.executeBase();
                    } else {
                        it.subCommands().stream().filter(it2 -> it2.name().equalsIgnoreCase(args[1]))
                                .findFirst()
                                .orElseThrow()
                                .onExecute()
                                .accept(Arrays.copyOfRange(args, 1, args.length));
                    }
                }, () -> SimpleLogger.info("This command does not exist!"));
    }

    public List<Command> similar(String context) {
        if(context.isEmpty()) {
            return this.commands;
        }

        List<Command> temp = new ArrayList<>();
        this.commands.forEach(it -> {
            if (it.name().toLowerCase().startsWith(context.toLowerCase()) || it.aliases().stream().anyMatch(it2 -> it2.toLowerCase().startsWith(context.toLowerCase()))) {
                temp.add(it);
            }
        });
        return temp;
    }
}
