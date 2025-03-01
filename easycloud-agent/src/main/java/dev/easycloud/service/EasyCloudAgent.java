package dev.easycloud.service;

import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.GroupProvider;
import dev.easycloud.service.group.SimpleGroupProvider;
import dev.easycloud.service.command.CommandProvider;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.platform.PlatformProvider;
import dev.easycloud.service.network.NetLineSecurity;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.service.SimpleServiceProvider;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.terminal.logger.LogType;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.server.NetServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudAgent {
    @Getter
    private static EasyCloudAgent instance;

    private final SimpleTerminal terminal;
    private final I18nProvider i18nProvider;
    private final CommandProvider commandProvider;

    private final ServiceProvider serviceProvider;
    private final GroupProvider groupProvider;
    private final PlatformProvider platformProvider;

    private final EasyCloudConfiguration configuration;
    private final NetServer netServer;

    @SneakyThrows
    public EasyCloudAgent() {
        instance = this;

        long timeSinceStart = System.currentTimeMillis();

        var localPath = Path.of("local");
        var resourcesPath = Path.of("resources");
        FileFactory.remove(localPath.resolve("services"));

        FileFactory.writeIfNotExists(localPath, new EasyCloudConfiguration());
        this.configuration = FileFactory.read(localPath, EasyCloudConfiguration.class);

        List.of("de", "en").forEach(s -> {
            try {
                var fileName = "i18n_" + s + ".properties";
                Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("i18n/" + fileName)), resourcesPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

        this.i18nProvider = new I18nProvider(this.configuration.locale());

        this.terminal = new SimpleTerminal();
        this.terminal.clear();

        this.netServer = Net.line().server();
        this.netServer
                .config(config -> {
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();

        this.netServer.withSecurityPolicy(new NetLineSecurity(this.configuration.key()));

        this.commandProvider = new CommandProvider();

        this.serviceProvider = new SimpleServiceProvider();
        this.groupProvider = new SimpleGroupProvider();
        this.platformProvider = new PlatformProvider();

        this.platformProvider.refresh();
        this.groupProvider.refresh();

        log.info(this.i18nProvider.get("netline.running", ansi().fgRgb(LogType.WHITE.rgb()).a("127.0.0.1").reset(), ansi().fgRgb(LogType.WHITE.rgb()).a("5200").reset()));

        log.info(this.i18nProvider.get("agent.found", ansi().fgRgb(LogType.WHITE.rgb()).a(this.groupProvider.groups().size() + " groups").reset()));
        log.info(this.i18nProvider.get("agent.found", ansi().fgRgb(LogType.WHITE.rgb()).a(this.platformProvider.platforms().size() + " platforms").reset()));

        log.info(this.i18nProvider.get("agent.startup", ansi().fgRgb(LogType.WHITE.rgb()).a((System.currentTimeMillis() - timeSinceStart)).a("ms").reset()));
        log.info(this.i18nProvider.get("agent.ready", ansi().fgRgb(LogType.PRIMARY.rgb()).a("help").reset()));

        this.terminal.start();
    }

    @SneakyThrows
    public void shutdown() {
        log.info(this.i18nProvider.get("services.shutdown.all"));

        for (Service service : new ArrayList<>(this.serviceProvider.services())) {
            service.shutdown();
        }

        log.info(this.i18nProvider.get("agent.shutdown"));

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        System.exit(0);
    }
}
