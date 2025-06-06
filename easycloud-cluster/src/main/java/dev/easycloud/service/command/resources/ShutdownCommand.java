package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;

public final class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("shutdown", "command.shutdown.info", "stop");
    }

    @Override
    public void executeBase() {
        EasyCloudCluster.instance().shutdown();
    }
}
