package dev.easycloud.service.group;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.terminal.logger.LogType;
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
public final class GroupProviderImpl implements GroupProvider {
    private final List<Group> groups;

    private final Path GROUPS_PATH = Path.of("resources").resolve("groups");

    public GroupProviderImpl() {
        this.groups = new ArrayList<>();

        this.scan();
    }

    public void scan() {
        this.groups.clear();

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

        var platformPath = Path.of("resources").resolve("platforms");
        if (!platformPath.toFile().exists()) {
            platformPath.toFile().mkdirs();
        }

        platforms.forEach(platform -> {
            var initializer = EasyCloudCluster.instance().platformProvider().initializers()
                    .stream()
                    .filter(it -> it.id().equals(platform.initializerId()))
                    .findFirst()
                    .orElseThrow();
            var jarPath = platformPath.resolve(platform.initializerId() + "-" + platform.version() + ".jar");

            if (!jarPath.toFile().exists()) {
                log.info(EasyCloudCluster.instance().i18nProvider().get("platform.downloading", ansi().fgRgb(LogType.PRIMARY.rgb()).a(platform.initializerId() + "-" + platform.version()).reset()));

                var downloadUrl = initializer.buildDownload(platform.version());
                if (downloadUrl == null) {
                    log.error(EasyCloudCluster.instance().i18nProvider().get("group.platform.download.failed", platform.initializerId() + "-" + platform.version()));
                }

                FileFactory.download(downloadUrl, jarPath);

                log.info("DEBUG 11");
                log.info(EasyCloudCluster.instance().i18nProvider().get("platform.ready", ansi().fgRgb(LogType.PRIMARY.rgb()).a(platform.initializerId() + "-" + platform.version()).reset()));
            }
        });
    }

    @Override
    @SneakyThrows
    public void create(Group group) {
        this.groups.add(group);

        this.refresh();
        var resourcesPath = Path.of("resources");
        var localPath = Path.of("local");
        var templatePath = localPath.resolve("templates").resolve(group.platform().type().equals(PlatformType.PROXY) ? "proxy" : "server").resolve(group.name());
        templatePath.toFile().mkdirs();

        if (!group.properties().saveFiles()) {
            new Thread(() -> {
                if (group.platform().initializerId().equals("paper")) {
                    try {
                        Files.copy(resourcesPath.resolve("platforms").resolve(group.platform().initializerId() + "-" + group.platform().version() + ".jar"), templatePath.resolve("tmp.jar"));
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
                log.info(EasyCloudCluster.instance().i18nProvider().get("group.created", Ansi.ansi().a(group.name()).fgRgb(LogType.WHITE.rgb()).reset()));
            }).start();
        } else {
            group.enabled(true);
            FileFactory.writeRaw(this.GROUPS_PATH.resolve(group.name() + ".json"), group);
            log.info(EasyCloudCluster.instance().i18nProvider().get("group.created", Ansi.ansi().a(group.name()).fgRgb(LogType.WHITE.rgb()).reset()));
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
