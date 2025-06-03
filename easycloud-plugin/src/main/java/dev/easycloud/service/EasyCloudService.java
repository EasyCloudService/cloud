package dev.easycloud.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.ServiceShutdownPacket;
import dev.easycloud.service.network.packet.request.RequestServiceInformationPacket;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.service.AdvancedServiceProvider;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.service.resources.Service;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.client.NetClient;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudService {
    @Getter
    private static EasyCloudService instance;

    private final NetClient netClient;
    private final EventProvider eventProvider;

    private AdvancedServiceProvider serviceProvider = null;

    public EasyCloudService(String key, String serviceId) {
        instance = this;

        this.netClient = Net.line().client();
        this.netClient
                .config(config -> {
                    config.id(key + "-" + serviceId);
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();
        this.eventProvider = new EventProvider(this.netClient);

        Event.registerTypeAdapter(Service.class, SimpleService.class);

        //System.out.println(FileFactory.GSON_NO_PRETTY.fromJson("{\"service\":{\"id\":\"Proxy-1\",\"group\":{\"enabled\":true,\"name\":\"Proxy\",\"platform\":{\"initializerId\":\"velocity\",\"version\":\"3.4.0-SNAPSHOT\",\"type\":\"PROXY\"},\"properties\":{\"memory\":512,\"maxPlayers\":10,\"always\":1,\"maximum\":1,\"isStatic\":false}},\"state\":\"STARTING\",\"port\":25565,\"directoryRaw\":\"local/services/Proxy-1\"}}", ServiceReadyEvent.class));

        this.eventProvider.subscribe(ServiceReadyEvent.class, (netChannel, event) -> {
            System.out.println("[DEBUG] Service is ready: " + event.service().id());
        });

        log.info("NetLine is connected to 127.0.0.1:5200.");
        /*this.netClient().track(ServiceInformationPacket.class, packet -> {
            this.serviceProvider = new SimpleServiceProvider(new SimpleService(packet.service().id(), packet.service().group(), packet.service().state(), packet.service().port(), packet.service().directoryRaw()));

            log.info("Received service information. Welcome {}", packet.service().id());
            packet.services().forEach(service -> {
                this.serviceProvider.services().add(new SimpleService(service.id(), service.group(), service.state(), service.port(), service.directoryRaw()));
            });
            EasyCloudService.instance().netClient().send(new ServiceReadyPacket(this.serviceProvider.thisService()));
        });*/

        log.info("Waiting for service information...");
        EasyCloudService.instance().netClient().send(new RequestServiceInformationPacket(serviceId));

        this.netClient().track(ServiceReadyPacket.class, packet -> {
            this.serviceProvider.services().add(new SimpleService(packet.service().id(), packet.service().group(), packet.service().state(), packet.service().port(), packet.service().directoryRaw()));
        });

        this.netClient().track(ServiceShutdownPacket.class, packet -> {
            this.serviceProvider.services().removeIf(it -> it.id().equals(packet.serviceId()));
        });
    }
}
