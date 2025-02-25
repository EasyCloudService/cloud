package dev.easycloud.service.dependency;

import dev.easycloud.service.terminal.SimpleTerminal;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public final class DependencyLoader {

    @SneakyThrows
    public DependencyLoader() {
        var executor = Executors.newCachedThreadPool();
        var manager = new DependencyManager(Path.of("storage").resolve("libaries"));

        SimpleTerminal.print("Updating libaries...");
        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt"));
        manager.downloadAll(executor, List.of(
                new StandardRepository("https://repo1.maven.org/maven2/"),
                new StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        )).join();

        SimpleTerminal.print("Libaries are up to date!");
    }
}
