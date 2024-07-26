package dev.easycloud.service.command.impl;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.terminal.logger.SimpleLogger;

import java.util.List;

public final class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "List all commands.", List.of());
    }

    @Override
    public void execute(String[] args) {
        EasyCloudAgent.instance().commandHandler().commands().forEach(it -> SimpleLogger.info(it.name() + " - " + it.description()));
    }
}
