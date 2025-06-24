package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.group.GroupProviderImpl;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "command.reload.info");
    }

    @Override
    public void executeBase() {
        EasyCloudClusterOld.instance().configuration().reload();
        log.info(this.i18nProvider().get("command.reload.configurations", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(EasyCloudClusterOld.instance().configuration().getPath()).reset()));

        EasyCloudClusterOld.instance().moduleService().search();
        log.info(this.i18nProvider().get("command.reload.modules", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(EasyCloudClusterOld.instance().moduleService().modules().size() + " modules").reset()));

        EasyCloudClusterOld.instance().platformProvider().search();
        log.info(this.i18nProvider().get("command.reload.platforms", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(EasyCloudClusterOld.instance().platformProvider().platforms().size() + " platforms").reset()));

        ((GroupProviderImpl) EasyCloudClusterOld.instance().groupProvider()).scan();
        log.info(this.i18nProvider().get("command.reload.groups", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(EasyCloudClusterOld.instance().groupProvider().groups().size() + " groups").reset()));

        //noinspection CodeBlock2Expr
        EasyCloudClusterOld.instance().groupProvider().groups().forEach(group -> {
            log.info(this.i18nProvider().get("command.reload.groups.found", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(group.getName()).reset()));
        });

        log.info(this.i18nProvider().get("command.reload.done"));

        EasyCloudClusterOld.instance().releasesService().check();
    }
}
