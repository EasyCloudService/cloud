package dev.easycloud.service.command.resources;

import com.google.inject.Inject;
import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.i18n.I18nProvider;

public final class ShutdownCommand extends Command {
    @Inject
    public ShutdownCommand(I18nProvider i18nProvider) {
        super("shutdown", i18nProvider.get("command.shutdown.info"));
    }

    @Override
    public void executeBase() {
        EasyCloudClusterOld.instance().shutdown();
    }
}
