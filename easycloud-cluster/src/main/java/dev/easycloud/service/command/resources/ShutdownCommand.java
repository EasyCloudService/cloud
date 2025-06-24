package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;

public final class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("shutdown", "command.shutdown.info");
    }

    @Override
    public void executeBase() {
        EasyCloudClusterOld.instance().shutdown();
    }
}
