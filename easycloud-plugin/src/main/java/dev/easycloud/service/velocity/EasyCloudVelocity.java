package dev.easycloud.service.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.proxy.RegisterServerPacket;
import dev.easycloud.service.network.packet.proxy.UnregisterServerPacket;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.client.NetClient;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;

@Plugin(id = "easycloud-velocity", name = "EasyCloud-Velocity", version = "1.0", description = "EasyCloud Velocity Plugin")
public final class EasyCloudVelocity {
    private final ProxyServer server;
    private final Logger logger;

    private final NetClient netClient;
    private final ServiceDataConfiguration configuration;

    @Inject
    public EasyCloudVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);

        this.netClient = Net.line().client();
        this.netClient
                .config(config -> {
                    config.id(this.configuration.key() + "-" + this.configuration.id());
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();

        logger.info("NetLine is connecting to 127.0.0.1:5200...");

        this.netClient.track(RegisterServerPacket.class, packet -> {
            this.server.registerServer(new ServerInfo(packet.id(), packet.address()));
            this.logger.info("Service {} is connected to port {}.", packet.id(), packet.address().getPort());
        });

        this.netClient.track(UnregisterServerPacket.class, packet -> {
            this.server.unregisterServer(this.server.getServer(packet.id()).orElseThrow().getServerInfo());
            this.logger.info("Service {} is disconnected.", packet.id());
        });
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.netClient.send(new ServiceReadyPacket(this.configuration.id(), this.server.getBoundAddress().getPort()));
    }
}
