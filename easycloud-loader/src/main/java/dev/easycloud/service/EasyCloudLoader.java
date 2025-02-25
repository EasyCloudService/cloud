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
        var classPathLoader = new ClassPathLoader();
        var storage = Path.of("storage");

        Thread.currentThread().setContextClassLoader(classPathLoader);

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
       // manager.relocateAll(executor).join();
        manager.loadAll(executor, classPathLoader).join();

        print("Extracting jars...");
        copyStreamFile("easycloud-api.jar", classPathLoader);
        copyStreamFile("easycloud-agent.jar", classPathLoader);
        copyStreamFile("easycloud-plugin.jar", classPathLoader);

        print("Booting EasyCloudAgent...");

        Class.forName("dev.easycloud.service.EasyCloudAgent", true, classPathLoader).getConstructor().newInstance();
        //System.out.println(Class.forName("dev.easycloud.service.packet.connection.ServiceConnectPacket"));
    }

    private static void print(String message) {
        System.out.println("[" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "] INFO: " + message);
    }

    private static void copyStreamFile(String name, ClassPathLoader classPathLoader) {
        var storage = Path.of("storage");
        try {
            var file = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
            if(file == null) {
                throw new RuntimeException("Resource " + name + " not found!");
            }
            Files.copy(file, storage.resolve("jars").resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        classPathLoader.appendFileToClasspath(storage.resolve("jars").resolve(name));
    }
}
