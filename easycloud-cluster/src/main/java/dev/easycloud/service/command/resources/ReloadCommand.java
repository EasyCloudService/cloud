package dev.easycloud.service.command.resources;

import com.google.inject.Inject;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.configuration.ClusterConfiguration;
import dev.easycloud.service.group.GroupProvider;
import dev.easycloud.service.group.GroupProviderImpl;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.module.ModuleService;
import dev.easycloud.service.platform.PlatformProvider;
import dev.easycloud.service.release.ReleasesService;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ReloadCommand extends Command {
    private final I18nProvider i18nProvider;
    private final ClusterConfiguration configuration;
    private final ModuleService moduleService;
    private final PlatformProvider platformProvider;
    private final GroupProvider groupProvider;
    private final ReleasesService releasesService;

    @Inject
    public ReloadCommand(I18nProvider i18nProvider, ClusterConfiguration configuration, ModuleService moduleService,
                         PlatformProvider platformProvider, GroupProvider groupProvider, ReleasesService releasesService) {
        super("reload", i18nProvider.get("command.reload.info"));
        this.i18nProvider = i18nProvider;
        this.configuration = configuration;
        this.moduleService = moduleService;
        this.platformProvider = platformProvider;
        this.groupProvider = groupProvider;
        this.releasesService = releasesService;
    }

    @Override
    public void executeBase() {
        this.configuration.reload();
        log.info(this.i18nProvider.get("command.reload.configurations", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.configuration.getPath()).reset()));

        this.moduleService.search();
        log.info(this.i18nProvider.get("command.reload.modules", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.moduleService.modules().size() + " modules").reset()));

        this.platformProvider.search();
        log.info(this.i18nProvider.get("command.reload.platforms", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.platformProvider.platforms().size() + " platforms").reset()));

        ((GroupProviderImpl) this.groupProvider).scan();
        log.info(this.i18nProvider.get("command.reload.groups", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.groupProvider.groups().size() + " groups").reset()));

        //noinspection CodeBlock2Expr
        this.groupProvider.groups().forEach(group -> {
            log.info(this.i18nProvider.get("command.reload.groups.found", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(group.getName()).reset()));
        });

        log.info(this.i18nProvider.get("command.reload.done"));

        this.releasesService.check();
    }
}
