package dev.easycloud.service;

import dev.easycloud.service.terminal.SimpleTerminal;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public final class DependencyLoader {

    @SneakyThrows
    public DependencyLoader() {
        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(Path.of("resources").resolve("libraries"));

        SimpleTerminal.print("Updating libraries...");
        manager.loadFromResource(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt")));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();

        SimpleTerminal.print("Libraries are up to date!");
    }
}
