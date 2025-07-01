package dev.easycloud.service.service.resources;

import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceStartingEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import dev.easycloud.service.network.event.resources.request.ServiceRequestShutdown;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import dev.easycloud.service.service.listener.ServiceUpdateListener;
import io.activej.inject.Injector;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public final class ServiceProviderImpl implements ServiceProvider {
    private final List<Service> services = new ArrayList<>();
    private final EventProvider eventProvider;

    public ServiceProviderImpl(EventProvider eventProvider) {
        this.eventProvider = eventProvider;
    }

    public void init(Injector inject) {
        inject.getInstance(ServiceUpdateListener.class);

        this.eventProvider.socket().read(ServiceStartingEvent.class, (socket, event) -> {
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
        this.eventProvider.publish(new ServiceRequestShutdown(service));
    }

    private final Map<UUID, CompletableFuture<Service>> serviceLaunchFutures = new HashMap<>();

    @Override
    public CompletableFuture<Service> launch(ServiceLaunchBuilder builder) {
        var future = new CompletableFuture<Service>();

        this.serviceLaunchFutures.put(builder.builderId(), future);
        this.eventProvider.publish(new ServiceRequestLaunch(builder));
        return future;
    }
}
