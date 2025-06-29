package dev.easycloud.service;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.Service;
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
        var service = CloudInjector.get().getInstance(Service.class);
        var eventProvider = CloudInjector.get().getInstance(EventProvider.class);
        eventProvider.publish(new ServiceReadyEvent(service));
        log.info("Service is now ready!");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        var service = CloudInjector.get().getInstance(Service.class);
        var eventProvider = CloudInjector.get().getInstance(EventProvider.class);
        eventProvider.publish(new ServiceShutdownEvent(service));
        eventProvider.close();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var service = CloudInjector.get().getInstance(Service.class);
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), Bukkit.getOnlinePlayers().size());
        service.publish();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var service = CloudInjector.get().getInstance(Service.class);
        service.addProperty(ServiceProperties.ONLINE_PLAYERS(), Bukkit.getOnlinePlayers().size() - 1);
        service.publish();
    }
}