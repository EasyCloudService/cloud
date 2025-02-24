package dev.easycloud.service;

import dev.easycloud.service.loader.ClassPathLoader;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public final class EasyCloudLoader {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    @SneakyThrows
    public static void main(String[] args) {
        var classLoader = new ClassPathLoader();
        var storage = Path.of("storage");

        print("Starting EasyCloudLoader...");

        storage.toFile().mkdirs();
        storage.resolve("jars").toFile().mkdirs();

        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(storage.resolve("dependencies"));

        print("Loading dependencies...");
        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt"));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();
        manager.loadAll(executor, classLoader).join();

        print("Extracting jars...");
        List.of("easycloud-agent.jar", "easycloud-api.jar", "easycloud-plugin.jar").forEach(it -> {
            try {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream(it), storage.resolve("jars").resolve(it), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            classLoader.appendFileToClasspath(storage.resolve("jars").resolve(it));
        });
        Thread.currentThread().setContextClassLoader(classLoader);

        print("Booting EasyCloudAgent...");
        Class.forName("dev.easycloud.service.EasyCloudAgent", true, classLoader).getConstructor().newInstance();
    }

    private static void print(String message) {
        System.out.println("[" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "] INFO: " + message);
    }
}
