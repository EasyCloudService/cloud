package dev.easycloud.service.module;

import dev.easycloud.service.platform.PlatformModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;

@Slf4j
public final class ModuleService {
    private final Path modulePath;

    public ModuleService() {
        this.modulePath = Path.of("modules");
        if (!this.modulePath.toFile().exists()) {
            this.modulePath.toFile().mkdirs();
        }
    }

    @SneakyThrows
    public void scan() {
        ModuleLoader.simple().clear();
        for (File file : this.modulePath.toFile().listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;

            // get file as jar file and scan all classes for Module annotations
            var jarFile = new JarFile(file);
            var entries = jarFile.entries();
            log.info("Scanning module: {}", file.getName());
            try (var classLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:" + file.getAbsolutePath() + "!/")})) {
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;

                    // load class and check for Module annotation
                    var className = entry.getName().replace('/', '.').replace(".class", "");
                    log.info("Loading class: {}", className);
                    Class<?> clazz = classLoader.loadClass(className);

                    if (clazz.isAnnotationPresent(PlatformModule.class)) {
                        /*var constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        var instance = constructor.newInstance();
                        if(instance instanceof Module module) {
                            ModuleLoader.simple().load(module);
                            continue;
                        }
                        log.warn("Class {} is annotated with @PlatformModule but does not implement Module interface.", className);*/
                        ModuleLoader.simple().add((Class<? extends Module>) clazz);
                    }
                }
            }
        }
    }
}