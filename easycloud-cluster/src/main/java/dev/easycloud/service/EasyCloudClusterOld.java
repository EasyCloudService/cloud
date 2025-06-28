package dev.easycloud.service;

import dev.easycloud.service.configuration.ClusterConfiguration;
import dev.easycloud.service.group.GroupProvider;
import dev.easycloud.service.command.CommandProvider;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.module.ModuleService;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.platform.PlatformProvider;
import dev.easycloud.service.release.ReleasesService;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.terminal.ClusterTerminal;
import dev.easycloud.service.terminal.TerminalState;
import io.activej.inject.Injector;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@Getter
@Accessors(fluent = true)
@Slf4j
@Deprecated
public final class EasyCloudClusterOld {
    @Getter
    private static EasyCloudClusterOld instance;

    private final ClusterTerminal terminal;
    private final I18nProvider i18nProvider;
    private final CommandProvider commandProvider;

    private final ServiceProvider serviceProvider;
    private final GroupProvider groupProvider;
    private final PlatformProvider platformProvider;
    private final ClusterConfiguration configuration;
    private final EventProvider eventProvider;
    private final ReleasesService releasesService;
    private final ModuleService moduleService;

    @SneakyThrows
    public EasyCloudClusterOld(Injector injector) {
        instance = this;

        this.terminal = injector.getInstance(ClusterTerminal.class);
        this.i18nProvider = injector.getInstance(I18nProvider.class);
        this.commandProvider = injector.getInstance(CommandProvider.class);
        this.serviceProvider = injector.getInstance(ServiceProvider.class);
        this.groupProvider = injector.getInstance(GroupProvider.class);
        this.platformProvider = injector.getInstance(PlatformProvider.class);
        this.configuration = injector.getInstance(ClusterConfiguration.class);
        this.eventProvider = injector.getInstance(EventProvider.class);
        this.releasesService = injector.getInstance(ReleasesService.class);
        this.moduleService = injector.getInstance(ModuleService.class);

        //new EasyCloudCluster().run();

        //long timeSinceStart = System.currentTimeMillis();

        //var localPath = Path.of("local");
        //var resourcesPath = Path.of("resources");
        //EasyFiles.Companion.remove(localPath.resolve("dynamic"));

        //var firstLaunch = !Files.exists(resourcesPath.resolve("config")) || !Files.exists(resourcesPath.resolve("groups"));
        //this.configuration = new ClusterConfiguration();
        /*List.of("de", "en").forEach(s -> {
            try {
                var fileName = "i18n_" + s + ".properties";
                Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("i18n/" + fileName)), resourcesPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

        this.i18nProvider = new I18nProvider();*/

        //this.terminal = new TerminalImpl();
        //this.terminal.start();

        //this.eventProvider = new EventProvider(new ServerSocket(this.configuration().security().value(), this.configuration.local().clusterPort()));
        //this.eventProvider.socket().waitForConnection().get();

       // log.info(this.i18nProvider.get("net.listening", ansi().fgRgb(Log4jColor.WHITE.rgb()).a("0.0.0.0").reset(), ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.configuration.local().clusterPort()).reset()));
        //Event.registerTypeAdapter(Service.class, ServiceImpl.class);

        //this.serviceProvider = new ServiceProviderImpl();
        //this.commandProvider = new CommandProvider();

        // onboarding if it's the first start
        /*if(firstLaunch) {
            var onboarding = new OnboardingProvider();
            onboarding.run();
            timeSinceStart = System.currentTimeMillis();
        }*/

        /*this.groupProvider = new GroupProviderImpl();
        this.platformProvider = new PlatformProvider();
        this.moduleService = new ModuleService();*/

        //this.platformProvider.search();
        /*var platformTypes = new StringBuilder();
        this.platformProvider.initializers().forEach(platform -> {
            if (!platformTypes.isEmpty()) platformTypes.append(", ");
            platformTypes.append(ansi().fgRgb(Log4jColor.WHITE.rgb()).a(platform.id()).reset());
        });
        log.info(this.i18nProvider.get("cluster.found", ansi().fgRgb(Log4jColor.WHITE.rgb()).a("platforms").reset(), platformTypes));

        this.groupProvider.refresh();
        var groups = new StringBuilder();
        this.groupProvider.groups().forEach(group -> {
            if (!groups.isEmpty()) groups.append(", ");
            groups.append(ansi().fgRgb(Log4jColor.WHITE.rgb()).a(group.getName().toLowerCase()).reset());
        });
        log.info(this.i18nProvider.get("cluster.found", ansi().fgRgb(Log4jColor.WHITE.rgb()).a("groups").reset(), groups));

        this.moduleService.refresh();

        this.releasesService = new ReleasesService();
        log.info(this.i18nProvider.get("cluster.ready", ansi().fgRgb(Log4jColor.WHITE.rgb()).a((System.currentTimeMillis() - timeSinceStart)).a("ms").reset()));*/
    }

    @SneakyThrows
    public void shutdown() {
        this.terminal.state(TerminalState.STOPPING);
        log.info(this.i18nProvider.get("services.shutdown.all"));

        for (var service: new ArrayList<>(this.serviceProvider.services())) {
            ((ServiceImpl) service).shutdown();
        }

        log.info(this.i18nProvider.get("cluster.shutdown"));

        if(Files.exists(Path.of("loader-patcher.jar"))) {
            log.info("Applying updates from loader-patcher.jar...");
        }

        this.terminal.readingThread().interrupt();
        this.terminal.terminal().close();

        if(Files.exists(Path.of("loader-patcher.jar"))) {
            new ProcessBuilder("java", "-jar", "dev.easycloud.patcher.jar").directory(Path.of("resources").resolve("libs").toFile()).start();
        }
        System.exit(0);
    }
}
