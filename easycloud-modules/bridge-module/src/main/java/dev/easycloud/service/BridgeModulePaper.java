package dev.easycloud.service;

import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
@AllArgsConstructor
public final class BridgeModulePaper extends JavaPlugin {

    @Override
    public void onEnable() {
        EasyCloudService.instance().eventProvider().publish(new ServiceReadyEvent(EasyCloudService.instance().serviceProvider().thisService()));
        log.info("Service is now ready!");
    }

    @Override
    public void onDisable() {
        EasyCloudService.instance().eventProvider().publish(new ServiceShutdownEvent(EasyCloudService.instance().serviceProvider().thisService()));
        EasyCloudService.instance().eventProvider().close();
    }
}