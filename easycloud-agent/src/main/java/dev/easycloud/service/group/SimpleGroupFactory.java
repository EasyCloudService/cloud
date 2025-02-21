package dev.easycloud.service.group;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
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

        log.info("Updating {} platform...", ansi().fgRgb(LogType.PRIMARY.rgb()).a(group.platform().id()).reset());
        var platformPath = Path.of("storage").resolve("platforms");
        if(!platformPath.toFile().exists()) {
            platformPath.toFile().mkdirs();
        }
        var initializer = EasyCloudAgent.instance().platformFactory().initializers()
                .stream()
                .filter(it -> it.id().equals(group.platform().initilizerId()))
                .findFirst()
                .orElseThrow();
        var download = initializer.buildDownload(group.platform().version());
        var jarPath = platformPath.resolve(group.platform().id() + ".jar");

        jarPath.toFile().delete();
        FileFactory.download(download, jarPath);
        log.info("Platform is now up to date.");

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
