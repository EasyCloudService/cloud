package dev.easycloud.service.network.event.resources;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.service.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceInformationEvent extends Event {
    private final Service service;
    private final List<Service> services;
}
