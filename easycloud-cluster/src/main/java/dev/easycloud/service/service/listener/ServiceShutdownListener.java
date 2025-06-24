package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;

public final class ServiceShutdownListener {

    public ServiceShutdownListener() {
        EasyCloudClusterOld.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            var service = EasyCloudClusterOld.instance().serviceProvider().get(event.service().id());
            if (service == null) {
                return;
            }

            EasyCloudClusterOld.instance().serviceProvider().shutdown(service);
        });
    }
}
