package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;

public final class ServiceUpdateListener {

    public ServiceUpdateListener() {
        EasyCloudService.instance().eventProvider().socket().read(ServiceUpdateEvent.class, (socket, event) -> {
            EasyCloudService.instance().serviceProvider().services().removeIf(it -> it.id().equals(event.service().id()));
            EasyCloudService.instance().serviceProvider().services().add(event.service());
        });
    }
}
