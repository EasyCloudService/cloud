package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.platform.PlatformType;

public final class ServiceShutdownListener {

    public ServiceShutdownListener() {
        EasyCloudAgent.instance().eventProvider().socket().read(ServiceShutdownEvent.class, (channel, event) -> {
            var service = EasyCloudAgent.instance().serviceProvider().get(event.service().id());
            if (service == null) {
                return;
            }

            if(service.group().platform().type().equals(PlatformType.SERVER)) {
                EasyCloudAgent.instance().eventProvider().publish(event);
            }
            EasyCloudAgent.instance().serviceProvider().shutdown(service);
        });
    }
}
