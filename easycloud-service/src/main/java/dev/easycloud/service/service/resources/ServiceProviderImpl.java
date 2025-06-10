package dev.easycloud.service.service.resources;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.network.event.resources.ServiceStartingEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import dev.easycloud.service.network.event.resources.request.ServiceRequestShutdown;
import dev.easycloud.service.service.InternalServiceProvider;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import dev.easycloud.service.service.listener.ServiceUpdateListener;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public final class ServiceProviderImpl implements InternalServiceProvider {
    private final List<Service> services = new ArrayList<>();
    private final String thisServiceId;

    public ServiceProviderImpl(final String thisServiceId) {
        this.thisServiceId = thisServiceId;

        new ServiceUpdateListener();

        EasyCloudService.instance().eventProvider().socket().read(ServiceStartingEvent.class, (socket, event) -> {
            if(this.serviceLaunchFutures.containsKey(event.builderId())) {
                this.serviceLaunchFutures.get(event.builderId()).complete(event.service());
                this.serviceLaunchFutures.remove(event.builderId());
            }
        });
    }

    @Override
    public Service get(String id) {
        return this.services.stream()
                .filter(it -> it.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void shutdown(Service service) {
        EasyCloudService.instance().eventProvider().publish(new ServiceRequestShutdown(service));
    }

    private final Map<UUID, CompletableFuture<Service>> serviceLaunchFutures = new HashMap<>();

    @Override
    public CompletableFuture<Service> launch(ServiceLaunchBuilder builder) {
        var future = new CompletableFuture<Service>();

        this.serviceLaunchFutures.put(builder.builderId(), future);
        EasyCloudService.instance().eventProvider().publish(new ServiceRequestLaunch(builder));
        return future;
    }

    @Override
    public Service thisService() {
        return this.services.stream()
                .filter(it -> it.id().equals(thisServiceId))
                .findFirst()
                .orElse(null);
    }
}
