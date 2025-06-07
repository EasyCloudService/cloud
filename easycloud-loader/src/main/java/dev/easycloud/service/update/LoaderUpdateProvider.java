package dev.easycloud.service.update;

import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.update.resources.GithubUpdateService;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LoaderUpdateProvider {

    @SneakyThrows
    public LoaderUpdateProvider() {
        SimpleTerminal.print("Checking for updates on github...");

        var updateService = new GithubUpdateService();
        var versionFile = Path.of("local").resolve("version.key");
        if (!Files.exists(versionFile)) {
            Files.write(versionFile, "empty".getBytes());
        }

        var currentVersion = Files.readString(versionFile);
        if (updateService.getInformation().getLatestVersion().equals(currentVersion)) {
            SimpleTerminal.print("No updates available, cloud is up to date.");
            return;
        }
        Files.deleteIfExists(versionFile);

        SimpleTerminal.print("Update is available. Version: " + updateService.getInformation().getLatestVersion() + " (Current: " + currentVersion + ")");

        updateService.download();

        SimpleTerminal.print("Unpacking update and replacing loader files...");
        SimpleTerminal.print("Please restart in 2 seconds.");
        Files.write(versionFile, updateService.getInformation().getLatestVersion().getBytes());
        new ProcessBuilder("java", "EasyCloudUpdater").directory(Path.of("resources").resolve("libraries").toFile()).start();
        System.exit(0);
    }
}