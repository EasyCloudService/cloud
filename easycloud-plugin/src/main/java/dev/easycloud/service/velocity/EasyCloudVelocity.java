package dev.easycloud.service.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.httpmarco.netline.Net;
import org.slf4j.Logger;

@Plugin(id = "easycloud-velocity", name = "EasyCloud Velocity", version = "1.0", description = "EasyCloud Velocity Plugin")
public final class EasyCloudVelocity {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public EasyCloudVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        Net.line()
                .client()
                .config(config -> {
                    config.hostname("0.0.0.0");
                    config.port(5200);
                });

        logger.info("NetLine is connecting to 0.0.0.0:5200...");
    }
}
