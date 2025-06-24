package dev.easycloud.service.service.listener;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceUpdateListener {

    public ServiceUpdateListener(ServiceProvider serviceProvider, EventProvider eventProvider) {
        eventProvider.socket().read(ServiceUpdateEvent.class, (netChannel, event) -> {
            var service = (ServiceImpl) event.service();
            var oldService = (ServiceImpl) serviceProvider.get(service.id());

            service.process(oldService.process());
            service.logStream(oldService.logStream());
            service.logCache(oldService.logCache());

            serviceProvider.services().removeIf(it -> it.id().equals(event.service().id()));
            serviceProvider.services().add(service);

            eventProvider.publish(event);
        });
    }
}
