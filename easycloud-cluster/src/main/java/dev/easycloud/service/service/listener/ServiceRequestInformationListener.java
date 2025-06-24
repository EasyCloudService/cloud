package dev.easycloud.service.service.listener;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import dev.easycloud.service.service.ServiceProvider;
import io.activej.bytebuf.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestInformationListener {

    public ServiceRequestInformationListener(ServiceProvider serviceProvider, EventProvider eventProvider) {
        //noinspection CodeBlock2Expr
        eventProvider.socket().read(ServiceRequestInformationEvent.class, (socket, event) -> {
            socket.write(ByteBuf.wrapForReading(new ServiceInformationEvent(serviceProvider.get(event.serviceId()), serviceProvider.services()).asBytes()));
        });
    }
}
