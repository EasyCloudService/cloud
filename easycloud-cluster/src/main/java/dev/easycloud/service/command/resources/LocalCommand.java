package dev.easycloud.service.command.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.release.ReleasesService;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class LocalCommand extends Command {
    private final I18nProvider i18nProvider;
    private final ReleasesService releasesService;

    @Inject
    public LocalCommand(I18nProvider i18nProvider, ReleasesService releasesService) {
        super("local", i18nProvider.get("command.local.info"));
        this.i18nProvider = i18nProvider;
        this.releasesService = releasesService;

        addSubCommand(new CommandNode("contributors", this.i18nProvider.get("command.local.contributors.info"), this::contributors));
        addSubCommand(new CommandNode("update", this.i18nProvider.get("command.local.update.info"), this::update));
    }

    @Override
    public void executeBase() {
        log.error(this.i18nProvider.get("global.wrongUsage"));
        log.info("local [contributors]");
        log.info("local [update]");
    }

    @SuppressWarnings("deprecation")
    private void contributors(String[] args) {
        log.info(this.i18nProvider.get("global.contributors"));
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
                    if (node.get("login") == null || node.get("type").asText().equalsIgnoreCase("bot")) {
                        return;
                    }

                    log.info(" - {} ({})", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a(node.get("login").asText()).reset(), node.get("contributions").asInt());
                });
            } catch (Exception ignored) {
            }
        }).start();

    }

    private void update(String[] args) {
        if (this.releasesService.CURRENT().equals(this.releasesService.name())) {
            log.info("You are using the latest version of EasyCloud. Good job :>");
            return;
        }

        log.info("Downloading version {} from Github...", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a(this.releasesService.name()).reset());
        new Thread(() -> {
            this.releasesService.download();

            log.info("―".repeat(80));
            log.info("");
            log.info("Download completed. Please restart the @{} to apply the changes.", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("EasyCloud").reset());
            log.info("After restarting wait {} for the {} to be applied.", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("4-5 seconds").reset(), ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("EasyCloud Updater").reset());
            log.info("");
            log.info("Always make sure you use {} command to stop the @{}", ansi().fgRgb(Log4jColor.ERROR.rgb()).a("shutdown").reset(), ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("EasyCloud").reset());
            log.info("And always make sure you read this: {}", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("https://github.com/EasyCloudService/cloud/releases/latest"));
            log.info("");
            log.info("―".repeat(80));
        }).start();
    }
}
