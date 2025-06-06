package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceUpdateListener {

    public ServiceUpdateListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceUpdateEvent.class, (netChannel, event) -> {
            EasyCloudCluster.instance().serviceProvider().services().removeIf(it -> it.id().equals(event.service().id()));
            EasyCloudCluster.instance().serviceProvider().services().add(event.service());

            EasyCloudCluster.instance().eventProvider().publish(event);
        });
    }
}
