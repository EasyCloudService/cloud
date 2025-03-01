package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.network.packet.ServiceShutdownPacket;
import dev.easycloud.service.network.packet.proxy.UnregisterServerPacket;
import dev.easycloud.service.platform.PlatformType;

public final class ServiceShutdownListener {

    public ServiceShutdownListener() {
        EasyCloudAgent.instance().netServer().track(ServiceShutdownPacket.class, packet -> {
            var service = EasyCloudAgent.instance().serviceProvider().get(packet.serviceId());
            if (service == null) {
                return;
            }

            if(service.group().platform().type().equals(PlatformType.SERVER)) {
                EasyCloudAgent.instance().netServer().broadcast(new UnregisterServerPacket(service.id()));
            }
            EasyCloudAgent.instance().serviceProvider().shutdown(service);
        });
    }
}
