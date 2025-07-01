package dev.easycloud.service.service.resources;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.network.event.EventProvider;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.Service;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@AllArgsConstructor
public final class ServiceImpl implements Service {
    public static EventProvider eventProvider;

    private final String id;
    private final Group group;

    private ServiceState state;
    private final String directoryRaw;

    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public void publish() {
        eventProvider.publish(new ServiceUpdateEvent(this));
    }

    @Override
    public void state(ServiceState state) {
        this.state = state;
    }
}
