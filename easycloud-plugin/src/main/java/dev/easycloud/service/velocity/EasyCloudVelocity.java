package dev.easycloud.service.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.packet.connection.ServiceConnectPacket;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.client.NetClient;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "easycloud-velocity", name = "EasyCloud Velocity", version = "1.0", description = "EasyCloud Velocity Plugin")
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

        this.netClient.send(new ServiceConnectPacket(this.configuration.id()));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
