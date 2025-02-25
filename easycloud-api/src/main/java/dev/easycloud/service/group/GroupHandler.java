package dev.easycloud.service.group;

import dev.easycloud.service.group.resources.Group;

import java.util.List;

public interface GroupHandler {
    void refresh();
    void create(Group group);
    Group get(String name);
    List<Group> groups();
}
