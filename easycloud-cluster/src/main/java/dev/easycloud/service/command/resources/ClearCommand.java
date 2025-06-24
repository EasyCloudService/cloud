package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;

public final class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "command.clear.info");
    }

    @Override
    public void executeBase() {
        EasyCloudClusterOld.instance().terminal().clear();
        EasyCloudClusterOld.instance().terminal().history().clear();
    }
}
