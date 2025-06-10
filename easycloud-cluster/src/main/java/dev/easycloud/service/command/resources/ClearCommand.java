package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;

public final class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "command.clear.info");
    }

    @Override
    public void executeBase() {
        EasyCloudCluster.instance().terminal().clear();
        EasyCloudCluster.instance().terminal().history().clear();
    }
}
