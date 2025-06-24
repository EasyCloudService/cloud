package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestLaunchListener {

    public ServiceRequestLaunchListener() {
        EasyCloudClusterOld.instance().eventProvider().socket().read(ServiceRequestLaunch.class, (socket, event) -> EasyCloudClusterOld.instance().serviceProvider().launch(event.builder()));
    }
}
