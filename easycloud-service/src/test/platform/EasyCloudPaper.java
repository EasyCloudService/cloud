package dev.easycloud.service.platform;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

@Slf4j
public final class EasyCloudPaper extends JavaPlugin {

    @Override
    public void onEnable() {
        var configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);
        new EasyCloudService(configuration.key(), configuration.clusterPort(), configuration.id());

        log.info(EasyCloudService.instance().serviceProvider().thisService().id());
    }

    @Override
    public void onDisable() {
        EasyCloudService.instance().eventProvider().publish(new ServiceShutdownEvent(EasyCloudService.instance().serviceProvider().thisService()));
        EasyCloudService.instance().eventProvider().close();
    }
}
