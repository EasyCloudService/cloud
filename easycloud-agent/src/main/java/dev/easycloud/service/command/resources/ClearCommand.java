package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;

import java.util.List;

public final class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "command.clear.info","clr");
    }

    @Override
    public void executeBase() {
        EasyCloudAgent.instance().terminal().clear();
        EasyCloudAgent.instance().terminal().history().clear();
    }
}
