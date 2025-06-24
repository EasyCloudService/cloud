package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import io.activej.bytebuf.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestInformationListener {

    public ServiceRequestInformationListener() {
        //noinspection CodeBlock2Expr
        EasyCloudClusterOld.instance().eventProvider().socket().read(ServiceRequestInformationEvent.class, (socket, event) -> {
            socket.write(ByteBuf.wrapForReading(new ServiceInformationEvent(EasyCloudClusterOld.instance().serviceProvider().get(event.serviceId()), EasyCloudClusterOld.instance().serviceProvider().services()).asBytes()));
        });
    }
}
