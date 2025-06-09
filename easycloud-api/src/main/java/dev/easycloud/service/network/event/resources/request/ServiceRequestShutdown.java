package dev.easycloud.service.network.event.resources.request;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.service.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceRequestShutdown extends Event {
    private final Service service;

}
