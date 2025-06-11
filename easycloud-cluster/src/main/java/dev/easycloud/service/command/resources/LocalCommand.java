package dev.easycloud.service.command.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class LocalCommand extends Command {

    public LocalCommand() {
        super("local", "command.local.info");

        addSubCommand(new CommandNode("contributors", "command.local.contributors.info", this::contributors));
        addSubCommand(new CommandNode("update", "command.local.update.info", this::update));
    }

    @Override
    public void executeBase() {
        log.error(this.i18nProvider().get("global.wrongUsage"));
        log.info("local [contributors]");
        log.info("local [update]");
    }

    private void contributors(String[] args) {
        log.info("Contributors: ");
        new Thread(() -> {
            URL url;
            try {
                url = new URL("https://api.github.com/repos/EasyCloudService/cloud/contributors");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            try {
                var mapper = new ObjectMapper().readTree(url);
                mapper.forEach(node -> {
                    if(node.get("login") == null || node.get("type").asText().equalsIgnoreCase("bot")) {
                        return;
                    }

                    log.info(" - {} ({})", ansi().fgRgb(LogType.PRIMARY.rgb()).a(node.get("login").asText()).reset(), node.get("contributions").asInt());
                });
            } catch (Exception ignored) {
            }
        }).start();

    }

    private void update(String[] args) {
        if (EasyCloudCluster.instance().releasesService().CURRENT().equals(EasyCloudCluster.instance().releasesService().name())) {
            log.info("You are using the latest version of EasyCloud. Good job :>");
            return;
        }

        log.info("Downloading version {} from Github...", ansi().fgRgb(LogType.PRIMARY.rgb()).a(EasyCloudCluster.instance().releasesService().name()).reset());
        new Thread(() -> {
            EasyCloudCluster.instance().releasesService().download();

            log.info("―".repeat(80));
            log.info("");
            log.info("Download completed. Please restart the @{} to apply the changes.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset());
            log.info("After restarting wait {} for the {} to be applied.", ansi().fgRgb(LogType.PRIMARY.rgb()).a("4-5 seconds").reset(), ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud Updater").reset());
            log.info("");
            log.info("―".repeat(80));
        }).start();
    }
}
