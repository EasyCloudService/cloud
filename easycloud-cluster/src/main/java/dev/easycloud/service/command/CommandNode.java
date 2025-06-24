package dev.easycloud.service.command;

import dev.easycloud.service.EasyCloudClusterOld;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@Accessors(fluent = true)
public final class CommandNode {
    private final String name;
    private final String description;

    @Getter(AccessLevel.NONE)
    private final Function<Void, List<String>> completer;
    private final Consumer<String[]> onExecute;

    public CommandNode(String name, String description, Consumer<String[]> onExecute) {
        this.name = name;
        this.description = EasyCloudClusterOld.instance().i18nProvider().get(description);
        this.completer = null;
        this.onExecute = onExecute;
    }

    public CommandNode(String name, String description, Function<Void, List<String>> completer, Consumer<String[]> onExecute) {
        this.name = name;
        this.description = EasyCloudClusterOld.instance().i18nProvider().get(description);
        this.completer = completer;
        this.onExecute = onExecute;
    }

    public List<String> completer() {
        if(this.completer == null) {
            return new ArrayList<>();
        }
        return this.completer.apply(null);
    }

}
