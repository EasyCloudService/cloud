package dev.easycloud.service.command;

import dev.easycloud.service.command.resources.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Getter
@Accessors(fluent = true)
public final class CommandHandler {
    private final List<Command> commands;

    public CommandHandler() {
        this.commands = new ArrayList<>();
        this.commands.addAll(List.of(new HelpCommand(), new ClearCommand(), new ShutdownCommand(), new GroupCommand(), new ServiceCommand()));
    }

    public void execute(String command, String[] args) {
        this.commands.stream()
                .filter(it -> it.name().equals(command) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(command)))
                .findFirst()
                .ifPresentOrElse(it -> {
                    if (args.length == 0 || it.subCommands().stream().noneMatch(it2 -> it2.name().equalsIgnoreCase(args[1]))) {
                        it.executeBase();
                    } else {
                        it.subCommands().stream().filter(it2 -> it2.name().equalsIgnoreCase(args[1]))
                                .findFirst()
                                .orElseThrow()
                                .onExecute()
                                .accept(Arrays.copyOfRange(args, 1, args.length));
                    }
                }, () -> {
                    log.error("This command does not exist!");
                });
    }
}
