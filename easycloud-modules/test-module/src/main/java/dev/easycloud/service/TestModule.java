package dev.easycloud.service;

import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import dev.easycloud.service.module.Module;
import dev.easycloud.service.platform.PlatformModule;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@PlatformModule(platformId = "velocity", name = "TestModule")
public class TestModule implements Module {

    @Override
    public void onLoad() {
        new ProxyPingEvent(null, null);

        System.out.println("TestModule-Proxy loaded successfully!");
    }

    @Override
    public void onDisable() {

    }
}