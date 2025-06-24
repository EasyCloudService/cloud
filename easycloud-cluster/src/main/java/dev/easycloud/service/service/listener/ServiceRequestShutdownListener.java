package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.network.event.resources.request.ServiceRequestShutdown;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestShutdownListener {

    public ServiceRequestShutdownListener() {
        //noinspection CodeBlock2Expr
        EasyCloudClusterOld.instance().eventProvider().socket().read(ServiceRequestShutdown.class, (socket, event) -> {
            EasyCloudClusterOld.instance().serviceProvider().shutdown(event.service().id());
        });
    }
}
