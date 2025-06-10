package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "command.help.info");
    }

    @Override
    public void executeBase() {
        EasyCloudCluster.instance().commandProvider().commands().forEach(it -> log.info("[{}] - {}", ansi().fgRgb(LogType.WHITE.rgb()).a(it.name()).reset(), it.description()));
    }
}
