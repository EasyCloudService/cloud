package dev.easycloud.service;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import dev.easycloud.service.network.socket.ClientSocket;
import dev.easycloud.service.service.ExtendedServiceProvider;
import dev.easycloud.service.service.resources.ServiceImpl;
import dev.easycloud.service.service.resources.ServiceProviderImpl;
import dev.easycloud.service.service.resources.Service;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudService {
    @Getter
    private static EasyCloudService instance;

    private final EventProvider eventProvider;

    private ExtendedServiceProvider serviceProvider = null;

    @SneakyThrows
    public EasyCloudService(String key, String serviceId) {
        instance = this;

        // Initialize the EasyCloudService
        this.eventProvider = new EventProvider(new ClientSocket(key));
        this.eventProvider.socket().waitForConnection().get();

        log.info("Requesting service information...");

        // Register adapters
        Event.registerTypeAdapter(Service.class, ServiceImpl.class);

        // Register events
        this.eventProvider.socket().read(ServiceInformationEvent.class, (netChannel, event) -> {
            this.serviceProvider = new ServiceProviderImpl(event.service());
            event.services().forEach(service -> this.serviceProvider.services().add(service));
            this.eventProvider.publish(new ServiceReadyEvent(event.service()));

            log.info("""
                     
                      ______                 _____ _                 _
                     |  ____|               / ____| |               | |
                     | |__   __ _ ___ _   _| |    | | ___  _   _  __| |
                     |  __| / _` / __| | | | |    | |/ _ \\| | | |/ _` |
                     | |___| (_| \\__ \\ |_| | |____| | (_) | |_| | (_| |
                     |______\\__,_|___/\\__, |\\_____|_|\\___/ \\__,_|\\__,_|
                                       __/ |
                                      |___/""");
            log.info("Welcome back, @{}.", event.service().id());

        });

        this.eventProvider.socket().read(ServiceReadyEvent.class, (netChannel, event) -> {
            if(event.service().id().equals(this.serviceProvider.thisService().id())) return;

            this.serviceProvider.services().add(event.service());
            log.info("Service '{}' is now online.", event.service().id());
        });

        this.eventProvider.socket().read(ServiceShutdownEvent.class, (netChannel, event) -> {
            if(event.service().id().equals(this.serviceProvider.thisService().id())) return;

            this.serviceProvider.services().removeIf(it -> it.id().equals(event.service().id()));
            log.info("Service '{}' has been shut down.", event.service().id());
        });


        // Request service information
        this.eventProvider.publish(new ServiceRequestInformationEvent(serviceId));
    }
}
