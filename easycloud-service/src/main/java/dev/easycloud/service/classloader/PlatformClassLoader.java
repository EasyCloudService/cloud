package dev.easycloud.service.classloader;

import lombok.SneakyThrows;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class PlatformClassLoader {

    @SneakyThrows
    public static Thread inject(Instrumentation instrumentation, String[] args) {
        var platformFile = Path.of("platform.jar").toAbsolutePath();
        var jarFile = new JarFile(platformFile.toFile());
        var classLoader = ClassLoader.getSystemClassLoader();

        // preload
        if(jarFile.getEntry("META-INF/versions.list") != null) {
            classLoader = new URLClassLoader(new URL[]{platformFile.toUri().toURL()}, classLoader);
            try (var inputStream = new JarInputStream(Files.newInputStream(platformFile))) {
                JarEntry jarEntry;
                while ((jarEntry = inputStream.getNextJarEntry()) != null) {
                    if (jarEntry.getName().endsWith(".class")) {
                        Class.forName(jarEntry.getName().replace('/', '.').replace(".class", ""), false, classLoader);
                    }
                }
            }
        }

        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        var mainClass = Class.forName(jarFile.getManifest().getMainAttributes().getValue("Main-Class"), true, classLoader);

        var thread = new Thread(() -> {
            try {
                mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            }
        });
        thread.setName("EasyCloudCluster");
        thread.setContextClassLoader(classLoader);
        thread.start();
        return thread;
    }
}
