package dev.easycloud.service;

import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.resources.ServiceProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
@AllArgsConstructor
public final class BridgeModulePaper extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        EasyCloudService.instance().eventProvider().publish(new ServiceReadyEvent(EasyCloudService.instance().serviceProvider().thisService()));
        log.info("Service is now ready!");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        EasyCloudService.instance().eventProvider().publish(new ServiceShutdownEvent(EasyCloudService.instance().serviceProvider().thisService()));
        EasyCloudService.instance().eventProvider().close();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var service = EasyCloudService.instance().serviceProvider().thisService();
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), Bukkit.getOnlinePlayers().size());
        service.publish();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var service = EasyCloudService.instance().serviceProvider().thisService();
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), Bukkit.getOnlinePlayers().size() - 1);
        service.publish();
    }
}