package dev.easycloud.service.group;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.file.FileFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SimpleGroupFactory implements GroupFactory {
    private final List<Group> groups;

    private final Path GROUPS_PATH = Path.of("storage").resolve("groups");

    public SimpleGroupFactory() {
        this.groups = new ArrayList<>();

        var pathFile = this.GROUPS_PATH.toFile();
        if(!pathFile.exists()) {
            pathFile.mkdirs();
        }

        for (File file : pathFile.listFiles()) {
            this.groups.add(FileFactory.readRaw(file.toPath(), Group.class));
        }
    }

    @Override
    public void create(Group group) {
        this.groups.add(group);
        FileFactory.writeRaw(this.GROUPS_PATH.resolve(group.name() + ".json"), group);
    }

    @Override
    public Group get(String name) {
        return this.groups.stream()
                .filter(it -> it.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Group> groups() {
        return this.groups;
    }
}
