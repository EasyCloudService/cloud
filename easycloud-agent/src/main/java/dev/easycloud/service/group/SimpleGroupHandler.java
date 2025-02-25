package dev.easycloud.service.group;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.terminal.LogType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class SimpleGroupHandler implements GroupHandler {
    private final List<Group> groups;

    private final Path GROUPS_PATH = Path.of("storage").resolve("groups");

    public SimpleGroupHandler() {
        this.groups = new ArrayList<>();

        var pathFile = this.GROUPS_PATH.toFile();
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        for (File file : pathFile.listFiles()) {
            this.groups.add(FileFactory.readRaw(file.toPath(), Group.class));
        }
    }

    @Override
    public void refresh() {
        List<Platform> platforms = new ArrayList<>();
        for (Group group : this.groups) {
            if (platforms.contains(group.platform())) {
                continue;
            }
            platforms.add(group.platform());
        }

        var platformPath = Path.of("storage").resolve("platforms");
        if (!platformPath.toFile().exists()) {
            platformPath.toFile().mkdirs();
        }

        platforms.forEach(platform -> {
            var initializer = EasyCloudAgent.instance().platformHandler().initializers()
                    .stream()
                    .filter(it -> it.id().equals(platform.initilizerId()))
                    .findFirst()
                    .orElseThrow();
            var jarPath = platformPath.resolve(platform.initilizerId() + "-" + platform.version() + ".jar");

            if (!jarPath.toFile().exists()) {
                log.info("Downloading {} platform...", ansi().fgRgb(LogType.PRIMARY.rgb()).a(platform.initilizerId() + "-" + platform.version()).reset());

                var downloadUrl = initializer.buildDownload(platform.version());
                if (downloadUrl == null) {
                    log.error("Failed to download platform. Invalid download url/version.");
                    return;
                }

                FileFactory.download(downloadUrl, jarPath);
                log.info("Platform {} is now ready.", ansi().fgRgb(LogType.PRIMARY.rgb()).a(platform.initilizerId() + "-" + platform.version()).reset());
            }
        });
    }

    @Override
    @SneakyThrows
    public void create(Group group) {
        this.groups.add(group);

        this.refresh();
        var storagePath = Path.of("storage");
        var templatePath = Path.of("template").resolve(group.platform().type().equals(PlatformType.PROXY) ? "proxy" : "server").resolve(group.name());
        templatePath.toFile().mkdirs();

        if (!group.data().isStatic()) {
            new Thread(() -> {
                if (group.platform().initilizerId().equals("paper")) {
                    try {
                        Files.copy(storagePath.resolve("platforms").resolve(group.platform().initilizerId() + "-" + group.platform().version() + ".jar"), templatePath.resolve("tmp.jar"));
                        var service = new ProcessBuilder("java", "-Dpaperclip.patchonly=true", "-jar", "tmp.jar")
                                .directory(templatePath.toFile())
                                .start();
                        service.waitFor();

                        Files.delete(templatePath.resolve("tmp.jar"));
                    } catch (IOException | InterruptedException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                group.enabled(true);
                FileFactory.writeRaw(this.GROUPS_PATH.resolve(group.name() + ".json"), group);
                log.info("{} has been created.", Ansi.ansi().a(group.name()).fgRgb(LogType.WHITE.rgb()).reset());
            }).start();
        } else {
            group.enabled(true);
            FileFactory.writeRaw(this.GROUPS_PATH.resolve(group.name() + ".json"), group);
            log.info("{} has been created.", Ansi.ansi().a(group.name()).fgRgb(LogType.WHITE.rgb()).reset());
        }
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
