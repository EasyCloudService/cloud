package dev.easycloud.service;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.easycloud.service.platform.PlatformModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@PlatformModule(platformId = "velocity", name = "BridgeModule")
public class BridgeModuleVelocity  {
    private final ProxyServer proxyServer;

}