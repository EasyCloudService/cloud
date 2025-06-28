package dev.easycloud.service.service.listener;

import com.google.inject.Inject;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.ServiceProvider;

public final class ServiceUpdateListener {
    @Inject
    public ServiceUpdateListener(EventProvider eventProvider, ServiceProvider serviceProvider) {
        eventProvider.socket().read(ServiceUpdateEvent.class, (socket, event) -> {
            serviceProvider.services().removeIf(it -> it.id().equals(event.service().id()));
            serviceProvider.services().add(event.service());
        });
    }
}
