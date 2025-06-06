package dev.easycloud.service.network.event.resources.request;

import dev.easycloud.service.network.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceRequestLaunch extends Event {
    private final String groupName;
    private final int count;

}
