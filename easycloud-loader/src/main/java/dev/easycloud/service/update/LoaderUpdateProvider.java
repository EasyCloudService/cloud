package dev.easycloud.service.update;

import dev.easycloud.service.EasyCloudVersion;
import dev.easycloud.service.terminal.SimpleTerminal;
import dev.easycloud.service.update.service.GithubUpdateService;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LoaderUpdateProvider {

    @SneakyThrows
    public LoaderUpdateProvider() {
        SimpleTerminal.print("Checking for updates on github...");

        var updateService = new GithubUpdateService();
        if (updateService.getInformation().latest().equals(EasyCloudVersion.VERSION)) {
            SimpleTerminal.print("No updates available, cloud is up to date.");
            return;
        }

        SimpleTerminal.print("Update is available. Version: " + updateService.getInformation().latest() + " (Current: " + EasyCloudVersion.VERSION + ")");

        updateService.download();

        SimpleTerminal.print("Unpacking update and replacing loader files...");
        SimpleTerminal.print("Please restart in 2 seconds.");
        new ProcessBuilder("java", "-jar", "dev.easycloud.patcher.jar").directory(Path.of("resources").resolve("libraries").toFile()).start();
        System.exit(0);
    }
}