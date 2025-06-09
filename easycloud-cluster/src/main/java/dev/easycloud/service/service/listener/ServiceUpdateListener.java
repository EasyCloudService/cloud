package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServiceUpdateListener {

    public ServiceUpdateListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceUpdateEvent.class, (netChannel, event) -> {
            var service = (ServiceImpl) event.service();
            var oldService = (ServiceImpl) EasyCloudCluster.instance().serviceProvider().get(service.id());

            service.process(oldService.process());
            service.logStream(oldService.logStream());
            service.logCache(oldService.logCache());

            EasyCloudCluster.instance().serviceProvider().services().removeIf(it -> it.id().equals(event.service().id()));
            EasyCloudCluster.instance().serviceProvider().services().add(service);

            EasyCloudCluster.instance().eventProvider().publish(event);
        });
    }
}
