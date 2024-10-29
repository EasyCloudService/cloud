package dev.easycloud.service;

import dev.easycloud.service.category.SimpleCategoryFactory;
import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import dev.httpmarco.evelon.layer.connection.ConnectionAuthenticationPath;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Accessors(fluent = true)
public final class EasyCloudAgent extends CloudDriver {
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final CommandHandler commandHandler;

    public EasyCloudAgent() {
        instance = this;

        var storagePath = Path.of("storage").toAbsolutePath();

        FileFactory.writeAsList(storagePath, new HikariConfiguration());
        ConnectionAuthenticationPath.set(storagePath.resolve("evelon.json").toString());

        this.terminal = new SimpleTerminal();
        this.commandHandler = new CommandHandler();

        this.categoryFactory = new SimpleCategoryFactory();
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
