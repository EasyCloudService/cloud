package dev.easycloud.service.platform;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.ServiceShutdownPacket;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class EasyCloudPaper extends JavaPlugin {
    private ServiceDataConfiguration configuration;

    @Override
    public void onEnable() {
        this.configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);

        new EasyCloudService(this.configuration.key(),this.configuration.id());
        this.getLogger().info("NetLine is connecting to 127.0.0.1:5200...");
    }

    @Override
    public void onDisable() {
        EasyCloudService.instance().netClient().send(new ServiceShutdownPacket(this.configuration.id()));
        EasyCloudService.instance().netClient().closeSync();
    }
}
