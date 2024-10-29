package dev.easycloud.service;

import dev.easycloud.service.category.CategoryFactory;
import dev.easycloud.service.category.SimpleCategoryFactory;
import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.file.FileFactory;
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
public final class EasyCloudAgent {
    @Getter
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final CommandHandler commandHandler;

    private final CategoryFactory categoryFactory;

    public EasyCloudAgent() {
        instance = this;

        long time = System.currentTimeMillis();
        var storagePath = Path.of("storage").toAbsolutePath();

        FileFactory.writeAsList(storagePath, new HikariConfiguration());
        ConnectionAuthenticationPath.set(storagePath.resolve("evelon.json").toString());

        this.terminal = new SimpleTerminal();
        this.terminal.clear();

        SimpleLogger.info("Cloud is starting... Some features may not work.");

        SimpleLogger.info("CommandHandler - Starting...");
        this.commandHandler = new CommandHandler();

        SimpleLogger.info("CategoryFactory - Starting...");
        this.categoryFactory = new SimpleCategoryFactory();

        SimpleLogger.info("");
        SimpleLogger.info("The cloud is ready. Type " + ansi().fgRgb(LoggerColor.PRIMARY.rgb()).a("help").reset() + " to get started.");
        SimpleLogger.info("Took " + ansi().fgRgb(LoggerColor.PRIMARY.rgb()).a((System.currentTimeMillis() - time)).a("ms").reset() + " to start.");
        SimpleLogger.info("");

        this.terminal.start();
    }

    @SneakyThrows
    public void shutdown() {
        SimpleLogger.warning("Shutting down...");

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }
}
