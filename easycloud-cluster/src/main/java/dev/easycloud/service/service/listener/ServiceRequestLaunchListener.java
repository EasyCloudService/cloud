package dev.easycloud.service.service.listener;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import dev.easycloud.service.service.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceRequestLaunchListener {

    public ServiceRequestLaunchListener(ServiceProvider serviceProvider, EventProvider eventProvider) {
        eventProvider.socket().read(ServiceRequestLaunch.class, (socket, event) -> serviceProvider.launch(event.builder()));
    }
}
