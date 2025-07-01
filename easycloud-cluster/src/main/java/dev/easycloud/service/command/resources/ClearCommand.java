package dev.easycloud.service.command.resources;

import dev.easycloud.service.command.Command;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.terminal.ClusterTerminal;
import io.activej.inject.annotation.Inject;

public final class ClearCommand extends Command {
    private final ClusterTerminal terminal;

    @Inject
    public ClearCommand(I18nProvider i18nProvider, ClusterTerminal terminal) {
        super("clear", i18nProvider.get("command.clear.info"));
        this.terminal = terminal;
    }

    @Override
    public void executeBase() {
        this.terminal.clear();
        this.terminal.history().clear();
    }
}
