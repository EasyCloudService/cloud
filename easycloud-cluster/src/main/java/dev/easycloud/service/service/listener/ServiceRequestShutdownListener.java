package dev.easycloud.service.service.listener;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.request.ServiceRequestShutdown;
import dev.easycloud.service.service.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestShutdownListener {

    public ServiceRequestShutdownListener(ServiceProvider serviceProvider, EventProvider eventProvider) {
        //noinspection CodeBlock2Expr
        eventProvider.socket().read(ServiceRequestShutdown.class, (socket, event) -> {
            serviceProvider.shutdown(event.service().id());
        });
    }
}
