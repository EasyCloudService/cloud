package dev.easycloud.service;

import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;
import lombok.SneakyThrows;
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

    @SneakyThrows
    public void shutdown() {
        SimpleLogger.warning("Shutting down...");

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }

    public static EasyCloudAgent instance() {
        return (EasyCloudAgent) instance;
    }
}
