package dev.easycloud.service.network.event.resources.request;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceRequestLaunch extends Event {
    private final ServiceLaunchBuilder builder;

}
