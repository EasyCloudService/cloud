package dev.easycloud.service.network.event.resources;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.service.resources.Service;
import lombok.*;

import java.net.InetSocketAddress;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceReadyEvent extends Event {
    private final Service service;
}
