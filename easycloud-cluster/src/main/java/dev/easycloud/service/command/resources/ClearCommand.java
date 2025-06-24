package dev.easycloud.service.command.resources;

import com.google.inject.Inject;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.terminal.Terminal;

public final class ClearCommand extends Command {
    private final Terminal terminal;

    @Inject
    public ClearCommand(I18nProvider i18nProvider, Terminal terminal) {
        super("clear", i18nProvider.get("command.clear.info"));
        this.terminal = terminal;
    }

    @Override
    public void executeBase() {
        this.terminal.clear();
        this.terminal.history().clear();
    }
}
