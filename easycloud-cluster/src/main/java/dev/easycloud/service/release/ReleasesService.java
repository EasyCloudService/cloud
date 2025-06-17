package dev.easycloud.service.release;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class ReleasesService {
    @Getter
    private final String CURRENT = "1.0.0-preview4";

    public ReleasesService() {
        new Thread(() -> {
            if(!this.CURRENT.equals(this.name()) && EasyCloudCluster.instance().configuration().local().announceUpdates()) {
               log.info("A new release available: {} | Current: {}", ansi().fgRgb(LogType.PRIMARY.rgb()).a(this.name()).reset(), ansi().fgRgb(LogType.ERROR.rgb()).a(this.CURRENT).reset());
               log.info("Use '{}' to update the cloud.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("local update").reset());
            }
        }).start();
    }

    private JsonNode node() {
        try {
            return new ObjectMapper().readTree(new URL("https://api.github.com/repos/EasyCloudService/cloud/releases/latest"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String name() {
        return this.node().get("name").asText();
    }

    public void download() {
        @SuppressWarnings("deprecation")
        var assets = this.node().get("assets");
        if (assets == null) {
            log.error("No assets found in the latest release.");
            return;
        }
        var downloadUrl = assets.get(0).get("browser_download_url").asText();

        //noinspection deprecation
        try (var in = new BufferedInputStream(new URL(downloadUrl).openStream());
             var fileOutputStream = new FileOutputStream("loader-patcher.jar")) {
            var dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
