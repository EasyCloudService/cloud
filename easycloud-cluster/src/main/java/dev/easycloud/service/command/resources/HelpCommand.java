package dev.easycloud.service.command.resources;

import com.google.inject.Inject;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandProvider;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class HelpCommand extends Command {
    private final CommandProvider commandProvider;

    @Inject
    public HelpCommand(I18nProvider i18nProvider, CommandProvider commandProvider) {
        super("help", i18nProvider.get("command.help.info"));
        this.commandProvider = commandProvider;
    }

    @Override
    public void executeBase() {
        this.commandProvider.commands().forEach(it -> log.info("[{}] - {}", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(it.name()).reset(), it.description()));
    }
}
