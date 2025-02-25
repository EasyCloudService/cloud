package dev.easycloud.service.update;

import dev.easycloud.service.terminal.SimpleTerminal;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

public class UpdateServiceHandler {

    @SneakyThrows
    public UpdateServiceHandler() {
        SimpleTerminal.clear();
        System.out.println("""
                  ┌──────────────────────────────────┐
                  │                                  │
                  │      Checking for update...      │
                  │                                  │
                  └──────────────────────────────────┘
                """);

        var updateService = new UpdateGithubService();
        var storage = Path.of("storage");
        var versionFile = storage.resolve("version.key");
        if (Files.exists(versionFile)) {
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
            new ProcessBuilder("java", "EasyCloudUpdater").directory(storage.resolve("libaries").toFile()).start();
            System.exit(0);
            return;
        }
        Files.write(versionFile, updateService.getInformation().getLatestVersion().getBytes());
    }
}