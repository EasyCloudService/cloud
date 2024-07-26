package dev.easycloud.service.command.impl;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.terminal.logger.SimpleLogger;

import java.util.List;

public final class ClearCommand extends Command {
    public ClearCommand() {
        super("clear", "Clear the terminal.", List.of("clr"));
    }

    @Override
    public void execute(String[] args) {
        EasyCloudAgent.instance().terminal().clear();
    }
}
