package dev.easycloud.service;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;


@Plugin(id = "hub-module", name = "EasyCloud HubModule", version = "1.0.0", description = "A hub module for EasyCloud on Velocity.", authors = "FlxwDNS")
public final class HubModuleVelocity {
    @Getter
    private static HubModuleVelocity instance;
    @Getter
    private final ProxyServer server;

    @Inject
    public HubModuleVelocity(ProxyServer server) {
        this.server = server;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.server.getCommandManager().register(this.server.getCommandManager().metaBuilder("hub")
                .aliases("lobby", "l", "h")
                .plugin(this)
                .build(), new HubCommand());
    }
}