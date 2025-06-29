package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestLaunchListener {

    public ServiceRequestLaunchListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceRequestLaunch.class, (socket, event) -> EasyCloudCluster.instance().serviceProvider().launch(event.builder()));
    }
}
