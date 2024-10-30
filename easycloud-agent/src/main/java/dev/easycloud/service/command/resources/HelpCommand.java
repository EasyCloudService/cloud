package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "List all commands.");
    }

    @Override
    public void executeBase() {
        EasyCloudAgent.instance().commandHandler().commands().forEach(it -> log.info("{} - {}", it.name(), it.description()));
    }
}
