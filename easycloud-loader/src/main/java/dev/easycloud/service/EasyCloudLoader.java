package dev.easycloud.service;

import dev.easycloud.service.loader.ClassPathLoader;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
public final class EasyCloudLoader {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    @SneakyThrows
    public static void main(String[] args) {
        var classPathLoader = new ClassPathLoader();
        var storage = Path.of("storage");

        Thread.currentThread().setContextClassLoader(classPathLoader);

        print("Starting EasyCloudLoader...");

        var logs = Path.of("logs");
        logs.toFile().mkdirs();
        storage.toFile().mkdirs();

        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(storage.resolve("dependencies"));

        print("Downloading dependencies...");
        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt"));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();

        print("Extracting files...");
        copyFile("easycloud-plugin.jar", storage.resolve("easycloud-plugin.jar"));
        copyFile("easycloud-api.jar", storage.resolve("dependencies").resolve("dev.easycloud.service-impl-stable.jar"));

        copyFile("easycloud-agent.jar", Path.of("easycloud-agent.jar"));

        print("Booting EasyCloudAgent...");
        var thread = new Thread(() -> {
            try {
                var process = new ProcessBuilder("java", "-Xms512M", "-Xmx512M", "-cp", "easycloud-agent.jar;storage/dependencies/*;", "dev.easycloud.service.EasyCloudBootstrap")
                        .redirectOutput(logs.resolve("latest.log").toFile())
                        .redirectError(logs.resolve("latest-error.log").toFile())
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .start();

                process.waitFor();
            } catch (IOException | InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        });
        thread.setDaemon(false);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(thread.isAlive()) {
                thread.interrupt();
            }
        }));

        while (true) {
            if(!thread.isAlive()) {
                System.exit(0);
                break;
            }
        }
    }

    private static void print(String message) {
        System.out.println("[" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "] INFO: " + message);
    }

    private static void copyFile(String name, Path path) {
        try {
            var file = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
            if (file == null) {
                throw new RuntimeException("Resource " + name + " not found!");
            }
            Files.copy(file, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
