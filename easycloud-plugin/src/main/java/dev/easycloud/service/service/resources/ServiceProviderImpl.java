package dev.easycloud.service.service.resources;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.network.event.resources.request.ServiceRequestLaunch;
import dev.easycloud.service.service.ExtendedServiceProvider;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.listener.ServiceUpdateListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class ServiceProviderImpl implements ExtendedServiceProvider {
    private final List<Service> services = new ArrayList<>();
    private final String thisServiceId;

    public ServiceProviderImpl(final String thisServiceId) {
        this.thisServiceId = thisServiceId;

        new ServiceUpdateListener();
    }

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
        EasyCloudService.instance().eventProvider().publish(new ServiceRequestLaunch(group.name(), count));
    }

    @Override
    public void launch(Group group) {
        this.launch(group, 1);
    }

    @Override
    public Service thisService() {
        return this.services.stream().filter(it -> it.id().equals(thisServiceId)).findFirst().orElseThrow();
    }
}
