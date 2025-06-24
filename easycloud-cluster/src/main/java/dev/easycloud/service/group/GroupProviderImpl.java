package dev.easycloud.service.group;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.files.EasyFiles;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class GroupProviderImpl implements GroupProvider {
    private final List<Group> groups;

    private final Path GROUPS_PATH = Path.of("resources").resolve("groups");

    public GroupProviderImpl() {
        this.groups = new ArrayList<>();

        this.scan();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void scan() {
        this.groups.clear();

        var pathFile = this.GROUPS_PATH.toFile();
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        for (File file : Objects.requireNonNull(pathFile.listFiles())) {
            if(file.isDirectory()) continue;
            this.groups.add(Configurations.Companion.readRaw(file.toPath(), Group.class));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void search() {
        List<Platform> platforms = new ArrayList<>();
        for (Group group : this.groups) {
            if (platforms.contains(group.getPlatform())) {
                continue;
            }
            platforms.add(group.getPlatform());
        }

        var platformPath = Path.of("resources").resolve("groups").resolve("platforms");
        if (!platformPath.toFile().exists()) {
            platformPath.toFile().mkdirs();
        }

        platforms.forEach(platform -> {
            var initializer = EasyCloudClusterOld.instance().platformProvider().initializers()
                    .stream()
                    .filter(it -> it.id().equals(platform.initializerId()))
                    .findFirst()
                    .orElseThrow();
            var jarPath = platformPath.resolve(platform.initializerId() + "-" + platform.version() + ".jar");

            if (!jarPath.toFile().exists()) {
                var serviceId = platform.initializerId() + "-" + platform.version();
                log.info(EasyCloudClusterOld.instance().i18nProvider().get("platform.downloading", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a(serviceId).reset()));

                var downloadUrl = initializer.buildDownload(platform.version());
                if (downloadUrl == null) {
                    log.error(EasyCloudClusterOld.instance().i18nProvider().get("group.platform.download.failed", serviceId));
                }

                if(downloadUrl == null) {
                    throw new IllegalStateException("Download URL for platform " + serviceId + " is null.");
                }

                EasyFiles.Companion.download(downloadUrl, jarPath);

                log.info(EasyCloudClusterOld.instance().i18nProvider().get("platform.ready", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a(serviceId).reset()));
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    @SneakyThrows
    public void create(Group group) {
        this.groups.add(group);

        this.search();
        var resourcesPath = Path.of("resources");
        var localPath = Path.of("local");
        var templatePath = localPath.resolve("templates").resolve(group.getPlatform().type().equals(PlatformType.PROXY) ? "proxy" : "server").resolve(group.getName());
        templatePath.toFile().mkdirs();

        if (!group.read(GroupProperties.SAVE_FILES())) {
            new Thread(() -> {
                if (group.getPlatform().initializerId().equals("paper")) {
                    try {
                        Files.copy(resourcesPath.resolve("groups").resolve("platforms").resolve(group.getPlatform().initializerId() + "-" + group.getPlatform().version() + ".jar"), templatePath.resolve("tmp.jar"));
                        var service = new ProcessBuilder("java", "-Dpaperclip.patchonly=true", "-jar", "tmp.jar")
                                .directory(templatePath.toFile())
                                .start();
                        service.waitFor();

                        Files.delete(templatePath.resolve("tmp.jar"));
                    } catch (IOException | InterruptedException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                group.setEnabled(true);
                Configurations.Companion.writeRaw(this.GROUPS_PATH.resolve(group.getName() + ".json"), group);
            }).start();
        } else {
            group.setEnabled(true);
            Configurations.Companion.writeRaw(this.GROUPS_PATH.resolve(group.getName() + ".json"), group);
        }
    }

    @Override
    public void delete(Group group) {
        this.groups.remove(group);

        try {
            Files.deleteIfExists(this.GROUPS_PATH.resolve(group.getName() + ".json"));
        } catch (IOException e) {
            log.error("Failed to delete group file: {}", group.getName(), e);
        }
    }

    @Override
    public Group get(String name) {
        return this.groups.stream()
                .filter(it -> it.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Group> groups() {
        return this.groups;
    }
}
