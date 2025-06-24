package dev.easycloud.service.service.command;

import com.google.inject.Inject;
import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.terminal.Terminal;
import dev.easycloud.service.terminal.TerminalImpl;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ServiceCommand extends Command {
    private final I18nProvider i18nProvider;
    private final ServiceProvider serviceProvider;
    private final Terminal terminal;

    @Inject
    public ServiceCommand(I18nProvider i18nProvider, ServiceProvider serviceProvider, Terminal terminal) {
        super("dev/easycloud/service", i18nProvider.get("command.service.info"));
        this.i18nProvider = i18nProvider;
        this.serviceProvider = serviceProvider;
        this.terminal = terminal;

        addSubCommand(new CommandNode("shutdown", "command.service.shutdown.info", unused -> this.serviceProvider.services()
                .stream()
                .map(Service::id)
                .toList(), this::shutdown)
        );
        addSubCommand(new CommandNode("screen", "command.service.screen.info", unused -> {
            return this.serviceProvider.services()
                    .stream()
                    .map(Service::id)
                    .toList();
        }, this::screen));
    }

    @Override
    public void executeBase() {
        log.error(this.i18nProvider.get("global.wrongUsage"));
        log.info("service [shutdown] [name]");
        log.info("service [screen] [name]");
    }

    private void shutdown(String[] args) {
        if (args.length < 2) {
            this.executeBase();
            return;
        }
        var service = EasyCloudClusterOld.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if (service == null) {
            log.error(i18nProvider.get("command.service.notFound"));
            return;
        }

        ((ServiceImpl) service).shutdown();
    }

    private void screen(String[] args) {
        if (args.length < 2) {
            this.executeBase();
            return;
        }
        var service = (ServiceImpl) EasyCloudClusterOld.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if (service == null) {
            log.error(i18nProvider.get("command.service.notFound"));
            return;
        }

        var terminal = (TerminalImpl) this.terminal;
        ((TerminalCompleter) terminal.lineReader().getCompleter()).enabled(false);

        terminal.logging(true);
        terminal.clear(false);

        service.logCache().forEach(service::print);

        service.logStream(true);
    }
}
