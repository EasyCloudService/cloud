package dev.easycloud.service.update;

import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.update.resources.GithubUpdateService;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LoaderUpdateProvider {

    @SneakyThrows
    public LoaderUpdateProvider() {
        SimpleTerminal.clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │      Checking for update...      │
                  │                                  │
                  └──────────────────────────────────┘
                """);


        var updateService = new GithubUpdateService();
        var versionFile = Path.of("local").resolve("version.key");
        if (!Files.exists(versionFile)) {
            Files.write(versionFile, "empty".getBytes());
        }

        var currentVersion = Files.readString(versionFile);
        if (updateService.getInformation().getLatestVersion().equals(currentVersion)) {
            SimpleTerminal.clear();
            System.out.println("""
                      ┌──────────────────────────────────┐
                      │                                  │
                      │       Cloud is up to date        │
                      │     No updates are available     │
                      │                                  │
                      └──────────────────────────────────┘
                    """);
            return;
        }
        Files.deleteIfExists(versionFile);

        SimpleTerminal.clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │   Update found! Downloading...   │
                  │                                  │
                  └──────────────────────────────────┘
                """);

        updateService.download();

        SimpleTerminal.clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │          Update finished         │
                  │        Please restart in 2s      │
                  │                                  │
                  └──────────────────────────────────┘
                """);
        Files.write(versionFile, updateService.getInformation().getLatestVersion().getBytes());
        new ProcessBuilder("java", "EasyCloudUpdater").directory(Path.of("resources").resolve("libaries").toFile()).start();
        System.exit(0);
        return;
    }
}