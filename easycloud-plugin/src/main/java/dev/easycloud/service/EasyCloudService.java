package dev.easycloud.service;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import dev.easycloud.service.service.AdvancedServiceProvider;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.service.SimpleServiceProvider;
import dev.easycloud.service.service.resources.Service;
import dev.httpmarco.netline.Net;
import dev.httpmarco.netline.client.NetClient;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

@Getter
@Accessors(fluent = true)
@Slf4j
public final class EasyCloudService {
    @Getter
    private static EasyCloudService instance;

    private final NetClient netClient;
    private final EventProvider eventProvider;

    private AdvancedServiceProvider serviceProvider = null;

    public EasyCloudService(String key, String serviceId) {
        instance = this;

        // Initialize the EasyCloudService
        this.netClient = Net.line().client();
        this.netClient
                .config(config -> {
                    config.id(key + "-" + serviceId);
                    config.hostname("127.0.0.1");
                    config.port(5200);
                })
                .bootSync();
        this.eventProvider = new EventProvider(this.netClient);

        System.out.println("NetLine has successfully connected to 127.0.0.1:5200.");
        System.out.println("Requesting service information...");

        // Register adapters
        Event.registerTypeAdapter(Service.class, SimpleService.class);

        // Register events
        this.eventProvider.subscribe(ServiceInformationEvent.class, (netChannel, event) -> {
            this.serviceProvider = new SimpleServiceProvider(event.service());
            event.services().forEach(service -> this.serviceProvider.services().add(service));
            this.eventProvider.publish(new ServiceReadyEvent(event.service()));

            System.out.println("""
                      ______                 _____ _                 _
                     |  ____|               / ____| |               | |
                     | |__   __ _ ___ _   _| |    | | ___  _   _  __| |
                     |  __| / _` / __| | | | |    | |/ _ \\| | | |/ _` |
                     | |___| (_| \\__ \\ |_| | |____| | (_) | |_| | (_| |
                     |______\\__,_|___/\\__, |\\_____|_|\\___/ \\__,_|\\__,_|
                                       __/ |
                                      |___/""");
            System.out.println("Welcome back, @" + event.service().id() + ".");
        });

        this.eventProvider.subscribe(ServiceReadyEvent.class, (netChannel, event) -> {
            if(event.service().id().equals(this.serviceProvider.thisService().id())) return;

            this.serviceProvider.services().add(event.service());
            System.out.println("Service '" + event.service().id() + "' is now online.");
        });

        this.eventProvider.subscribe(ServiceShutdownEvent.class, (netChannel, event) -> {
            if(event.service().id().equals(this.serviceProvider.thisService().id())) return;

            this.serviceProvider.services().removeIf(it -> it.id().equals(event.service().id()));
            System.out.println("Service '" + event.service().id() + "' has been shut down.");
        });


        // Request service information
        this.eventProvider.publish(new ServiceRequestInformationEvent(serviceId));
    }
}
