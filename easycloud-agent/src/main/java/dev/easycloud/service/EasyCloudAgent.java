package dev.easycloud.service;

import dev.easycloud.service.category.CategoryFactory;
import dev.easycloud.service.category.SimpleCategoryFactory;
import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Accessors(fluent = true)
@Slf4j
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

        this.terminal = new SimpleTerminal();
        this.terminal.clear();

        log.info("Cloud is starting... Some features may not work.");

        log.info("CommandHandler - Starting...");
        this.commandHandler = new CommandHandler();

        log.info("CategoryFactory - Starting...");
        this.categoryFactory = new SimpleCategoryFactory();

        this.terminal.clear();
        log.info("The cloud is ready. Type {} to get started.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("help").reset());
        log.info("Took {} to start.", ansi().fgRgb(LogType.PRIMARY.rgb()).a((System.currentTimeMillis() - time)).a("ms").reset());

        this.terminal.start();
    }

    @SneakyThrows
    public void shutdown() {
        log.debug("Shutting down...");

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }
}
