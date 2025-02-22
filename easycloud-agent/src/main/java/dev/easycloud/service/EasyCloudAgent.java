package dev.easycloud.service;

import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.GroupFactory;
import dev.easycloud.service.group.SimpleGroupFactory;
import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.platform.PlatformFactory;
import dev.easycloud.service.service.ServiceFactory;
import dev.easycloud.service.service.SimpleServiceFactory;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudAgent {
    @Getter
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final CommandHandler commandHandler;

    private final ServiceFactory serviceFactory;
    private final GroupFactory groupFactory;
    private final PlatformFactory platformFactory;

    public EasyCloudAgent() {
        instance = this;

        long timeSinceStart = System.currentTimeMillis();
        FileFactory.removeDirectory(Path.of("services"));

        this.terminal = new SimpleTerminal();
        this.terminal.clear();

        this.commandHandler = new CommandHandler();

        this.serviceFactory = new SimpleServiceFactory();
        this.groupFactory = new SimpleGroupFactory();

        log.info("{} were found.", ansi().fgRgb(LogType.WHITE.rgb()).a(this.groupFactory.groups().size() + " groups").reset());

        this.platformFactory = new PlatformFactory();
        this.platformFactory.refresh();

        log.info("{} were found.", ansi().fgRgb(LogType.WHITE.rgb()).a(this.platformFactory.platforms().size() + " platforms").reset());

        //this.terminal.clear();
        log.info("It took {} to start the cloud.", ansi().fgRgb(LogType.WHITE.rgb()).a((System.currentTimeMillis() - timeSinceStart)).a("ms").reset());
        log.info("The cloud is ready. Type {} to get started.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("help").reset());

        this.terminal.start();
    }

    @SneakyThrows
    public void shutdown() {
        log.info("Shutting down services...");

        for (Service service : new ArrayList<>(this.serviceFactory.services())) {
            service.shutdown();
        }

        log.info("Shutting down... Goodbye!");

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }
}
