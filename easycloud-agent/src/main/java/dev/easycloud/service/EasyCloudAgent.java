package dev.easycloud.service;

import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.terminal.SimpleTerminal;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class EasyCloudAgent extends CloudDriver {
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final CommandHandler commandHandler;

    public EasyCloudAgent() {
        instance = this;

        this.terminal = new SimpleTerminal();
        this.commandHandler = new CommandHandler();
    }

    public static EasyCloudAgent instance() {
        return (EasyCloudAgent) instance;
    }
}
