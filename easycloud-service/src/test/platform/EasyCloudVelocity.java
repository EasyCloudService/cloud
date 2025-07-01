package dev.easycloud.service.platform;

import jakarta.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.configuration.FileFactory;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.service.resources.ServiceProperties;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;

@Plugin(id = "easycloud-velocity", name = "EasyCloud-Velocity", version = "1.0", description = "EasyCloud Velocity Plugin", authors = "EasyCloud")
public final class EasyCloudVelocity {
    private final ProxyServer server;

    @SuppressWarnings("CodeBlock2Expr")
    @Inject
    public EasyCloudVelocity(ProxyServer server, Logger logger) {
        this.server = server;

        this.server.getAllServers().forEach(it -> server.unregisterServer(it.getServerInfo()));

        var configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);
        logger.info("EasyCloudService is starting...");

        new EasyCloudService(configuration.key(), configuration.clusterPort(), configuration.id());

        EasyCloudService.instance()
                .serviceProvider()
                .services()
                .stream()
                .filter(it -> it.state().equals(ServiceState.ONLINE) && !it.id().equals(EasyCloudService.instance().serviceProvider().thisService().id()))
                .forEach(service -> this.server.registerServer(new ServerInfo(service.id(), new InetSocketAddress(service.property(ServiceProperties.PORT())))));

        EasyCloudService.instance().eventProvider().socket().read(ServiceReadyEvent.class, (channel, event) -> {
            logger.info("[DEBUG] Service {} is ready", event.service().id());

            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.registerServer(new ServerInfo(event.service().id(), new InetSocketAddress(event.service().property(ServiceProperties.PORT()))));
        });

        EasyCloudService.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            logger.info("[DEBUG] Service {} is shutting down", event.service().id());

            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.unregisterServer(this.server.getServer(event.service().id()).orElseThrow().getServerInfo());
        });
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.server.getAllServers().stream()
                .filter(it -> it.getServerInfo().getName().toLowerCase().startsWith("lobby"))
                .findFirst()
                .ifPresentOrElse(event::setInitialServer, () -> event.getPlayer().disconnect(Component.text("§cNo fallback server found!")));
    }
}
