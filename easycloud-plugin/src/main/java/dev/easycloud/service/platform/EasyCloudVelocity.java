package dev.easycloud.service.platform;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.proxy.RegisterServerPacket;
import dev.easycloud.service.network.packet.proxy.UnregisterServerPacket;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "easycloud-velocity", name = "EasyCloud-Velocity", version = "1.0", description = "EasyCloud Velocity Plugin", authors = "EasyCloud")
public final class EasyCloudVelocity {
    private final ProxyServer server;
    private final Logger logger;

    private final ServiceDataConfiguration configuration;

    @Inject
    public EasyCloudVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.server.getAllServers().forEach(it -> server.unregisterServer(it.getServerInfo()));

        this.configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);

        this.logger.info("EasyCloudService is starting...");

        new EasyCloudService(this.configuration.key(), this.configuration.id());

        EasyCloudService.instance().netClient().track(RegisterServerPacket.class, packet -> {
            this.server.registerServer(new ServerInfo(packet.id(), packet.address()));
            this.logger.info("Service '{}' is online on port {}.", packet.id(), packet.address().getPort());
        });

        EasyCloudService.instance().netClient().track(UnregisterServerPacket.class, packet -> {
            this.server.unregisterServer(this.server.getServer(packet.id()).orElseThrow().getServerInfo());
            this.logger.info("Service '{}' is disconnected.", packet.id());
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
