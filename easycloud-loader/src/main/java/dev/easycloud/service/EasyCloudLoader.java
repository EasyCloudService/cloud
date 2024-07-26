package dev.easycloud.service;

import dev.easycloud.service.loader.ClassPathLoader;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Executors;

public final class EasyCloudLoader {

    @SneakyThrows
    public static void main(String[] args) {
        var classLoader = new ClassPathLoader();
        var storage = Path.of("storage");

        System.out.println("[-] INFO: Starting EasyCloudLoader...");

        storage.toFile().mkdirs();
        storage.resolve("jars").toFile().mkdirs();

        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(storage.resolve("dependencies"));

        System.out.println("[-] INFO: Loading dependencies...");
        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt"));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();
        manager.loadAll(executor, classLoader).join();

        System.out.println("[-] INFO: Extracting jars...");
        List.of("easycloud-agent.jar", "easycloud-api.jar").forEach(it -> {
            try {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream(it), storage.resolve("jars").resolve(it), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            classLoader.appendFileToClasspath(storage.resolve("jars").resolve(it));
        });
        Thread.currentThread().setContextClassLoader(classLoader);

        System.out.println("[-] INFO: Booting EasyCloudAgent...");
        Class.forName("dev.easycloud.service.EasyCloudAgent", true, classLoader).getConstructor().newInstance();
    }
}
