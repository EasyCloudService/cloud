package dev.easycloud.service;

import dev.easycloud.service.terminal.SimpleTerminal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
public final class EasyCloudBoot {

    @SneakyThrows
    public static void main(String[] args) {
        var local = Path.of("local");
        var resources = Path.of("resources");
        var libraries = resources.resolve("libraries");
        local.toFile().mkdirs();
        resources.toFile().mkdirs();
        libraries.toFile().mkdirs();

        if(Files.exists(Path.of("loader-patcher.jar"))) {
            SimpleTerminal.print("An update is in progress, please wait...");
            SimpleTerminal.print("If its been more than 1 minute, contact the EasyCloud support team.");
            System.exit(0);
        }

        copyFile("easycloud-patcher.jar", libraries.resolve("dev.easycloud.patcher.jar"));

        new DependencyLoader();

        copyFile("easycloud-service.jar", resources.resolve("easycloud-service.jar"));
        copyFile("easycloud-api.jar", libraries.resolve("dev.easycloud.api.jar"));

        copyFile("easycloud-cluster.jar", Path.of("easycloud-cluster.jar"));
        var modules = Path.of("modules");
        modules.toFile().mkdirs();
        copyFile("bridge-module.jar", modules.resolve("bridge-module.jar"));

        var thread = processThread();

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

    private static @NotNull Thread processThread() {
        var thread = new Thread(() -> {
            try {
                var fileArg = "easycloud-cluster.jar;resources/libraries/*;";
                if(!isWindows()) {
                    fileArg = fileArg.replace(";", ":");
                }

                var process = new ProcessBuilder("java", "-Xms512M", "-Xmx512M", "--enable-native-access=ALL-UNNAMED", "-cp", fileArg, "dev.easycloud.service.EasyCloudBoot")
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .start();

                process.waitFor();
            } catch (IOException | InterruptedException exception) {
                if(exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    System.exit(1);
                    return;
                }

                throw new RuntimeException(exception);
            }
        });
        thread.setDaemon(false);
        thread.start();
        return thread;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
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
