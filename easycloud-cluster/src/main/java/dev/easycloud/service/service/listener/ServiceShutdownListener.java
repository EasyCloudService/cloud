package dev.easycloud.service.service.listener;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.service.ServiceProvider;

public final class ServiceShutdownListener {

    public ServiceShutdownListener(ServiceProvider serviceProvider, EventProvider eventProvider) {
        eventProvider.socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            var service = serviceProvider.get(event.service().id());
            if (service == null) {
                return;
            }

            serviceProvider.shutdown(service);
        });
    }
}
