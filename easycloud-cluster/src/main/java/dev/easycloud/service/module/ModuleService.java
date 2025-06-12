package dev.easycloud.service.module;

import dev.easycloud.service.platform.PlatformModule;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

@Slf4j
public final class ModuleService {
    private final Path modulePath;
    @Getter
    private final Map<PlatformModule, Path> modules;

    public ModuleService() {
        this.modulePath = Path.of("modules");
        this.modules = new HashMap<>();
        if (!this.modulePath.toFile().exists()) {
            this.modulePath.toFile().mkdirs();
        }
    }

    @SneakyThrows
    public void refresh() {
        for (File file : this.modulePath.toFile().listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;

            var jarFile = new JarFile(file);
            var entries = jarFile.entries();
            try (var classLoader = URLClassLoader.newInstance(new URL[]{file.toPath().toAbsolutePath().toUri().toURL()}, ClassLoader.getSystemClassLoader())) {
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;

                    // load class and check for Module annotation
                    var className = entry.getName().replace('/', '.').replace(".class", "");

                    var clazz = Class.forName(className, false, classLoader);
                    if (clazz.isAnnotationPresent(PlatformModule.class)) {
                        var platformModule = clazz.getAnnotation(PlatformModule.class);
                        this.modules.put(platformModule, file.toPath());
                    }
                }
            }
        }
        log.info("Found following modules:");
        this.modules.forEach((module, path) -> {
            log.info(" - {}:{}", module.name(), module.platformId());
        });
    }
}