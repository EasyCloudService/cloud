package dev.easycloud.service.command.impl;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;

import java.util.List;

public final class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("shutdown", "Shutdown the cloud.", List.of("stop"));
    }

    @Override
    public void execute(String[] args) {
        EasyCloudAgent.instance().shutdown();
    }
}
