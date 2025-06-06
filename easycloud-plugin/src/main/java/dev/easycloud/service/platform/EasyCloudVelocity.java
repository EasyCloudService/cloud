package dev.easycloud.service.platform;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.service.resources.property.DefaultProperty;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;

@Plugin(id = "easycloud-velocity", name = "EasyCloud-Velocity", version = "1.0", description = "EasyCloud Velocity Plugin", authors = "EasyCloud")
public final class EasyCloudVelocity {
    private final ProxyServer server;
    private final Logger logger;

    private final ServiceDataConfiguration configuration;

    @Inject
    public EasyCloudVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.server.getAllServers().forEach(it -> server.unregisterServer(it.getServerInfo()));

        this.configuration = FileFactory.read(Path.of(""), ServiceDataConfiguration.class);

        this.logger.info("EasyCloudService is starting...");

        new EasyCloudService(this.configuration.key(), this.configuration.id());

        EasyCloudService.instance().eventProvider().socket().read(ServiceInformationEvent.class, (channel, event) -> {
            event.services()
                    .stream()
                    .filter(it -> it.state().equals(ServiceState.ONLINE) && !it.id().equals(EasyCloudService.instance().serviceProvider().thisService().id()))
                    .forEach(service -> this.server.registerServer(new ServerInfo(service.id(), new InetSocketAddress(service.property(DefaultProperty.PORT())))));
        });

        EasyCloudService.instance().eventProvider().socket().read(ServiceReadyEvent.class, (channel, event) -> {
            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.registerServer(new ServerInfo(event.service().id(), new InetSocketAddress(event.service().property(DefaultProperty.PORT()))));
        });

        EasyCloudService.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            if (event.service().id().equals(EasyCloudService.instance().serviceProvider().thisService().id())) return;
            this.server.unregisterServer(this.server.getServer(event.service().id()).orElseThrow().getServerInfo());
        });
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.server.getAllServers().stream()
                .filter(it -> it.getServerInfo().getName().toLowerCase().startsWith("lobby"))
                .findFirst()
                .ifPresentOrElse(event::setInitialServer, () -> event.getPlayer().disconnect(Component.text("§cNo fallback server found!")));
    }
}
