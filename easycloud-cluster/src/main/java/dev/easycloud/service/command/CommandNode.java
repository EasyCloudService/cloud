package dev.easycloud.service.command;

import dev.easycloud.service.EasyCloudCluster;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Accessors(fluent = true)
public final class CommandNode {
    private final String name;
    private final String description;

    private final Consumer<String[]> onExecute;
    private final List<CommandNode> commandNodes = new ArrayList<>();

    public CommandNode(String name, String description, Consumer<String[]> onExecute) {
        this.name = name;
        this.description = EasyCloudCluster.instance().i18nProvider().get(description);
        this.onExecute = onExecute;
    }

    public CommandNode addCommandNode(CommandNode commandNode) {
        commandNodes.add(commandNode);
        return this;
    }
}
