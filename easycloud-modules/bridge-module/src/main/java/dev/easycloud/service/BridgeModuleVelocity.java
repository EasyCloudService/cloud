package dev.easycloud.service;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.resources.ServiceProperties;
import dev.easycloud.service.service.resources.ServiceState;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;

@Plugin(id = "bridge-module", name = "EasyCloud BridgeModule", version = "1.0.0", description = "A bridge module for EasyCloud on Velocity")
public class BridgeModuleVelocity  {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public BridgeModuleVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        EasyCloudService.instance()
                .serviceProvider()
                .services()
                .stream()
                .filter(it -> it.state().equals(ServiceState.ONLINE) && !it.id().equals(EasyCloudService.instance().serviceProvider().thisService().id()))
                .forEach(service -> this.server.registerServer(new ServerInfo(service.id(), new InetSocketAddress(service.property(ServiceProperties.PORT())))));

        EasyCloudService.instance().eventProvider().socket().read(ServiceReadyEvent.class, (channel, event) -> {
            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.registerServer(new ServerInfo(event.service().id(), new InetSocketAddress(event.service().property(ServiceProperties.PORT()))));
        });

        EasyCloudService.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.unregisterServer(this.server.getServer(event.service().id()).orElseThrow().getServerInfo());
        });
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        EasyCloudService.instance().eventProvider().publish(new ServiceReadyEvent(EasyCloudService.instance().serviceProvider().thisService()));
        this.logger.info("Service is now ready!");
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.server.getAllServers().stream()
                .filter(it -> it.getServerInfo().getName().toLowerCase().startsWith("lobby"))
                .findFirst()
                .ifPresentOrElse(event::setInitialServer, () -> event.getPlayer().disconnect(Component.text("§cNo fallback server found!")));
    }

}