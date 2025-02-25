package dev.easycloud.service;

import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
        var storage = Path.of("storage");
        var libaries = storage.resolve("libaries");
        storage.toFile().mkdirs();
        libaries.toFile().mkdirs();

        clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │      Checking for update...      │
                  │                                  │
                  └──────────────────────────────────┘
                """);
        clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │       Cloud is up to date        │
                  │                                  │
                  └──────────────────────────────────┘
                """);

        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(libaries);

        print("Updating libaries...");
        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt"));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();
        print("Libaries are up to date!");

        copyFile("easycloud-plugin.jar", storage.resolve("easycloud-plugin.jar"));
        copyFile("easycloud-api.jar", libaries.resolve("dev.easycloud.service-impl-stable.jar"));

        copyFile("easycloud-agent.jar", Path.of("easycloud-agent.jar"));

        var thread = new Thread(() -> {
            try {
                var fileArg = "easycloud-agent.jar;storage/libaries/*;";
                if(!isWindows()) {
                    fileArg = fileArg.replace(";", ":");
                }

                var process = new ProcessBuilder("java", "-Xms512M", "-Xmx512M", "-cp", fileArg, "dev.easycloud.service.EasyCloudBootstrap")
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

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void print(String message) {
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
