package dev.easycloud.service.service.resources;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.ExtendedService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@AllArgsConstructor
public final class ServiceImpl implements ExtendedService {
    private final String id;
    private final Group group;

    private ServiceState state;
    private final String directoryRaw;

    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public void publish() {
        EasyCloudService.instance().eventProvider().publish(new ServiceUpdateEvent(this));
    }

    @Override
    public void state(ServiceState state) {
        this.state = state;
    }
}
