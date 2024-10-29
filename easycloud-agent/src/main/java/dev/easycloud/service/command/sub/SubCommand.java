package dev.easycloud.service.command.sub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class SubCommand {
    private final String name;
    private final String description;

    private final Consumer<String[]> onExecute;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public SubCommand addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
        return this;
    }
}
