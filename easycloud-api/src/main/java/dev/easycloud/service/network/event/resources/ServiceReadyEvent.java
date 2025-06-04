package dev.easycloud.service.network.event.resources;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.service.resources.Service;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceReadyEvent extends Event {
    private final String test;
    private final Platform platform;
    private final Service service;
}
