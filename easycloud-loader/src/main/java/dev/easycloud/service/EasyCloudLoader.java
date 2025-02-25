package dev.easycloud.service;

import dev.easycloud.service.dependency.DependencyLoader;
import dev.easycloud.service.update.UpdateServiceHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@Slf4j
public final class EasyCloudLoader {

    @SneakyThrows
    public static void main(String[] args) {
        var storage = Path.of("storage");
        var libaries = storage.resolve("libaries");
        storage.toFile().mkdirs();
        libaries.toFile().mkdirs();

        copyFile("EasyCloudUpdater.class", libaries.resolve("EasyCloudUpdater.class"));

        if(Arrays.stream(args).toList().stream().anyMatch(arg -> arg.equals("-Dauto.updates=true"))) {
            new UpdateServiceHandler();
        } else {
            System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │  (Auto)Updates are not enabled!  │
                  │                                  │
                  └──────────────────────────────────┘
                  To enable them, open the 'start-script' and add the following argument 'eaup' to the end.
                """);
        }

        new DependencyLoader();

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
