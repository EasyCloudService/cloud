package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.platform.PlatformType;

public final class ServiceShutdownListener {

    public ServiceShutdownListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            var service = EasyCloudCluster.instance().serviceProvider().get(event.service().id());
            if (service == null) {
                return;
            }

            EasyCloudCluster.instance().serviceProvider().shutdown(service);
        });
    }
}
