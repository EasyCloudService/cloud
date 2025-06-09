package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.group.GroupProviderImpl;
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
        EasyCloudCluster.instance().platformProvider().refresh();
        ((GroupProviderImpl) EasyCloudCluster.instance().groupProvider()).scan();

        log.info(this.i18nProvider().get("command.reload.platforms", ansi().fgRgb(LogType.WHITE.rgb()).a(EasyCloudCluster.instance().platformProvider().platforms().size() + " platforms").reset()));
        log.info(this.i18nProvider().get("command.reload.groups", ansi().fgRgb(LogType.WHITE.rgb()).a(EasyCloudCluster.instance().groupProvider().groups().size() + " groups").reset()));

        //noinspection CodeBlock2Expr
        EasyCloudCluster.instance().groupProvider().groups().forEach(group -> {
            log.info(this.i18nProvider().get("command.reload.groups.found", ansi().fgRgb(LogType.WHITE.rgb()).a(group.name()).reset()));
        });

        log.info(this.i18nProvider().get("command.reload.done"));
    }
}
