package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;

import java.util.List;

public interface ServiceFactory {
    List<Service> services();

    void launch(Group group);
    void launch(Group group, int count);

}
