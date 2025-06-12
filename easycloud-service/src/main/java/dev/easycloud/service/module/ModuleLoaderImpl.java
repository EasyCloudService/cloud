package dev.easycloud.service.module;

import dev.easycloud.service.EasyCloudService;
import dev.easycloud.service.EasyCloudServiceBoot;
import dev.easycloud.service.platform.PlatformModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

@Slf4j
public class ModuleLoaderImpl {

    @SneakyThrows
    public ModuleLoaderImpl() {
        var modulePath = Path.of("").toAbsolutePath().getParent().getParent().getParent().resolve("modules");
        log.info(modulePath.toString());
        for (File file : modulePath.toFile().listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;

            var jarFile = new JarFile(file);
            var entries = jarFile.entries();
            try (var classLoader = URLClassLoader.newInstance(new URL[]{file.toPath().toAbsolutePath().toUri().toURL()}, ClassLoader.getSystemClassLoader())) {
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;

                    // load class and check for Module annotation
                    var className = entry.getName().replace('/', '.').replace(".class", "");
                    this.loadModule(Class.forName(className, false, classLoader), file);
                }
            }
        }
    }

    private void loadModule(Class<?> clazz, File file) throws IOException {
        if (!clazz.isAnnotationPresent(PlatformModule.class)) {
            return;
        }

        var platformModule = clazz.getAnnotation(PlatformModule.class);
        if (!platformModule.platformId().equals(EasyCloudService.instance().serviceProvider().thisService().group().platform().initializerId())) {
            return;
        }
        var plugins = Path.of("plugins");
        Files.copy(file.toPath(), plugins);
    }

}
