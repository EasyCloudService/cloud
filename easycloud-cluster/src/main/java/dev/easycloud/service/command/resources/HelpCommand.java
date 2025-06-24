package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "command.help.info");
    }

    @Override
    public void executeBase() {
        EasyCloudClusterOld.instance().commandProvider().commands().forEach(it -> log.info("[{}] - {}", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(it.name()).reset(), it.description()));
    }
}
