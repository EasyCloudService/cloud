package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.request.ServiceRequestShutdown;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestShutdownListener {

    public ServiceRequestShutdownListener() {
        //noinspection CodeBlock2Expr
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceRequestShutdown.class, (socket, event) -> {
            EasyCloudCluster.instance().serviceProvider().shutdown(event.service().id());
        });
    }
}
