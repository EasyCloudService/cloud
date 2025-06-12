package dev.easycloud.service;

import dev.easycloud.service.platform.PlatformModule;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
@PlatformModule(platformId = "paper", name = "BridgeModule")
public final class BridgeModulePaper extends JavaPlugin {

    @Override
    public void onEnable() {
        
    }

    @Override
    public void onDisable() {

    }
}