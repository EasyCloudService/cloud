package dev.easycloud.service;

import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.GroupHandler;
import dev.easycloud.service.group.SimpleGroupHandler;
import dev.easycloud.service.command.CommandHandler;
import dev.easycloud.service.network.packet.ServiceConnectPacket;
import dev.easycloud.service.platform.PlatformHandler;
import dev.easycloud.service.network.NetLineSecurity;
import dev.easycloud.service.service.ServiceHandler;
import dev.easycloud.service.service.SimpleServiceHandler;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.LogType;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.server.NetServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudAgent {
    @Getter
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final CommandHandler commandHandler;

    private final ServiceHandler serviceHandler;
    private final GroupHandler groupHandler;
    private final PlatformHandler platformHandler;

    private final String privateKey;
    private final NetServer netServer;

    @SneakyThrows
    public EasyCloudAgent() {
        instance = this;

        this.privateKey = "key-" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000000, 99999999);

        long timeSinceStart = System.currentTimeMillis();
        FileFactory.removeDirectory(Path.of("services"));

        this.terminal = new SimpleTerminal();
        this.terminal.clear();

        this.netServer = Net.line().server();
        this.netServer
                .config(config -> {
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();

        this.netServer.withSecurityPolicy(new NetLineSecurity(this.privateKey));
        this.netServer.track(ServiceConnectPacket.class, (channel, packet) -> {
            System.out.println("Service connected: " + packet.serviceId());
        });

        log.info("NetLine is running on {}:{}.", ansi().fgRgb(LogType.WHITE.rgb()).a("127.0.0.1").reset(), ansi().fgRgb(LogType.WHITE.rgb()).a("5200").reset());

        this.commandHandler = new CommandHandler();

        this.serviceHandler = new SimpleServiceHandler();
        this.groupHandler = new SimpleGroupHandler();

        log.info("{} were found.", ansi().fgRgb(LogType.WHITE.rgb()).a(this.groupHandler.groups().size() + " groups").reset());

        this.platformHandler = new PlatformHandler();
        this.platformHandler.refresh();

        log.info("{} were found.", ansi().fgRgb(LogType.WHITE.rgb()).a(this.platformHandler.platforms().size() + " platforms").reset());

        this.groupHandler.refresh();

        log.info("It took {} to start the cloud.", ansi().fgRgb(LogType.WHITE.rgb()).a((System.currentTimeMillis() - timeSinceStart)).a("ms").reset());
        log.info("The cloud is ready. Type {} to get started.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("help").reset());

        this.terminal.start();
    }

    @SneakyThrows
    public void shutdown() {
        log.info("Shutting down services...");

        for (Service service : new ArrayList<>(this.serviceHandler.services())) {
            service.shutdown();
        }

        log.info("Shutting down... Goodbye!");

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }
}
