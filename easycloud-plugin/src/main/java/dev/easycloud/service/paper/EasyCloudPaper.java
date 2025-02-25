package dev.easycloud.service.paper;

import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.client.NetClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class EasyCloudPaper extends JavaPlugin {
    private NetClient netClient;
    private ServiceDataConfiguration configuration;

    @Override
    public void onEnable() {
        this.configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);

        this.netClient = Net.line().client();
        this.netClient
                .config(config -> {
                    config.id(this.configuration.key() + "-" + this.configuration.id());
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();

        this.getLogger().info("NetLine is connecting to 127.0.0.1:5200...");

        this.netClient.send(new ServiceReadyPacket(this.configuration.id(), Bukkit.getPort()));
    }
}
