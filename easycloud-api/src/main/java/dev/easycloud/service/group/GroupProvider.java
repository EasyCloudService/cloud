package dev.easycloud.service.group;

import dev.easycloud.service.group.resources.Group;

import java.util.List;

public interface GroupProvider {
    void refresh();
    void create(Group group);
    void delete(Group group);
    Group get(String name);
    List<Group> groups();
}
