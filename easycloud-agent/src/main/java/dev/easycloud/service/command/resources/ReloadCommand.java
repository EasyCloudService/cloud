package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.group.SimpleGroupProvider;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "command.reload.info", "refresh");
    }

    @Override
    public void executeBase() {
        EasyCloudAgent.instance().platformProvider().refresh();
        ((SimpleGroupProvider) EasyCloudAgent.instance().groupProvider()).scan();

        log.info(this.i18nProvider().get("command.reload.platforms", ansi().fgRgb(LogType.WHITE.rgb()).a(EasyCloudAgent.instance().platformProvider().platforms().size() + " platforms").reset()));
        log.info(this.i18nProvider().get("command.reload.groups", ansi().fgRgb(LogType.WHITE.rgb()).a(EasyCloudAgent.instance().groupProvider().groups().size() + " groups").reset()));

        EasyCloudAgent.instance().groupProvider().groups().forEach(group -> {
            log.info(this.i18nProvider().get("command.reload.groups.found", ansi().fgRgb(LogType.WHITE.rgb()).a(group.name()).reset()));
        });

        log.info(this.i18nProvider().get("command.reload.done"));
    }
}
