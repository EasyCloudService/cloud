package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.network.packet.ServiceReadyPacket;
import dev.easycloud.service.network.packet.proxy.RegisterServerPacket;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceReadyListener {

    public ServiceReadyListener() {
        EasyCloudAgent.instance().netServer().track(ServiceReadyPacket.class, (client, packet) -> {
            var service = EasyCloudAgent.instance().serviceProvider().get(packet.serviceId());
            if (service == null) {
                return;
            }

            service.state(ServiceState.ONLINE);
            if(service.group().platform().type().equals(PlatformType.SERVER)) {
                EasyCloudAgent.instance().netServer().broadcast(new RegisterServerPacket(service.id(), new InetSocketAddress(service.port())));
            }
            if(service.group().platform().type().equals(PlatformType.PROXY)) {
                EasyCloudAgent.instance().serviceProvider().services().forEach(it -> {
                    client.send(new RegisterServerPacket(it.id(), new InetSocketAddress(it.port())));
                });
            }
            log.info("Service {} is now ready.", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset());
        });
    }
}
