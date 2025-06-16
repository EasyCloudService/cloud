package dev.easycloud.service.module;

import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class ModuleService {
    private final Path modulePath;
    @Getter
    private final Map<ModuleConfiguration, Path> modules;

    public ModuleService() {
        this.modulePath = Path.of("modules");
        this.modules = new HashMap<>();
        if (!this.modulePath.toFile().exists()) {
            this.modulePath.toFile().mkdirs();
        }
    }

    @SneakyThrows
    public void refresh() {
        this.modules.clear();
        for (File file : Objects.requireNonNull(this.modulePath.toFile().listFiles())) {
            if (!file.getName().endsWith(".jar")) continue;

            var jarFile = new JarFile(file);
            try (var classLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + file.getAbsolutePath() + "!/")})) {
                var configurationPath = this.modulePath.resolve(file.getName() + ".json");
                Files.copy(Objects.requireNonNull(classLoader.getResourceAsStream("module.json")), configurationPath, StandardCopyOption.REPLACE_EXISTING);
                var configuration = Configurations.Companion.readRaw(configurationPath, ModuleConfiguration.class);
                Files.deleteIfExists(configurationPath);
                this.modules.put(configuration, file.toPath());
            }
        }
        if(this.modules.isEmpty()) {
            log.info("No modules found in the 'modules' directory.");
            return;
        }

        log.info("Found following modules:");
        this.modules.forEach((module, path) -> {
            var platforms = new StringBuilder();
            Arrays.stream(module.platforms()).toList().forEach(platform -> {
                if (!platforms.isEmpty()) platforms.append(";");
                platforms.append(ansi().fgRgb(LogType.PRIMARY.rgb()).a(platform).reset());
            });

            log.info("* {} ({})", ansi().fgRgb(LogType.PRIMARY.rgb()).a(module.name()).reset(), platforms);
        });
    }
}