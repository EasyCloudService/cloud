package dev.easycloud.service;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import dev.easycloud.service.network.socket.ClientSocket;
import dev.easycloud.service.service.InternalServiceProvider;
import dev.easycloud.service.service.resources.ServiceImpl;
import dev.easycloud.service.service.resources.ServiceProviderImpl;
import dev.easycloud.service.service.Service;
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
    private final InternalServiceProvider serviceProvider;

    @SneakyThrows
    public EasyCloudService(String key, int port, String serviceId) {
        instance = this;

        // Initialize the EasyCloudService
        this.eventProvider = new EventProvider(new ClientSocket(key, port));
        this.eventProvider.socket().waitForConnection().get();

        this.serviceProvider = new ServiceProviderImpl(serviceId);

        log.info("Requesting service information...");

        // Register adapters
        Event.registerTypeAdapter(Service.class, ServiceImpl.class);

        // Register events
        new Thread(() -> {
            this.eventProvider.socket().read(ServiceInformationEvent.class, (netChannel, event) -> {
                event.services().forEach(service -> this.serviceProvider.services().add(service));
                this.eventProvider.publish(new ServiceReadyEvent(event.service()));

                log.info("""
                     
                            _            _
                           |_  _.  _    /  |  _       _|
                           |_ (_| _> \\/ \\_ | (_) |_| (_|
                                     /
                           Welcome back, @SERVICE_ID""".replace("SERVICE_ID", event.service().id()));
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
        }).start();

        // Request service information
        this.eventProvider.publish(new ServiceRequestInformationEvent(serviceId));

        while (this.serviceProvider.thisService() == null) {
            log.info("Waiting for service information...");
            //noinspection BusyWait
            Thread.sleep(1000);
        }
    }
}
