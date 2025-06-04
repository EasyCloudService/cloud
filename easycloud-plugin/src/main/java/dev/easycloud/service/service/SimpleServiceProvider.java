package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class SimpleServiceProvider implements AdvancedServiceProvider {
    private final List<Service> services = new ArrayList<>();
    private final Service thisService;

    @Override
    public Service get(String id) {
        return this.services.stream()
                .filter(it -> it.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void shutdown(Service service) {
        //EasyCloudService.instance().netClient().send(new RequestServiceShutdownPacket(service.id()));
    }

    @Override
    public void launch(Group group, int count) {
        //EasyCloudService.instance().netClient().send(new RequestServiceLaunchPacket(group.name(), count));
    }

    @Override
    public void launch(Group group) {
        this.launch(group, 1);
    }

}
