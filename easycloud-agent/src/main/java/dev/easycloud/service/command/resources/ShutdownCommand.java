package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;

import java.util.List;

public final class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("shutdown", "Shutdown the cloud.", "stop");
    }

    @Override
    public void executeBase() {
        EasyCloudAgent.instance().shutdown();
    }
}
