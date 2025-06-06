package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;

import java.util.List;

public interface ServiceProvider {
    List<Service> services();

    Service get(String id);

    default void shutdown(String id) {
        var service = this.get(id);
        if (service != null) {
            this.shutdown(service);
        } else {
            throw new IllegalArgumentException("Service with id " + id + " not found.");
        }
    }
    void shutdown(Service service);

    void launch(Group group);
    default void launch(Group group, int count) {
        for (int i = 0; i < count; i++) {
            this.launch(group);
        }
    }

}
