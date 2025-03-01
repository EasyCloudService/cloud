package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;

import java.util.List;

public interface ServiceProvider {
    List<Service> services();

    Service get(String id);

    void shutdown(Service service);

    void launch(Group group);
    void launch(Group group, int count);

}
