package dev.easycloud.service.service.command;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ServiceCommand extends Command {
    public ServiceCommand() {
        super("dev/easycloud/service", "command.service.info");

        addSubCommand(new CommandNode("shutdown", "command.service.shutdown.info", unused -> {
            return EasyCloudClusterOld.instance().serviceProvider().services().stream().map(Service::id).toList();
        }, this::shutdown));
        addSubCommand(new CommandNode("screen", "command.service.screen.info", unused -> {
            return EasyCloudClusterOld.instance().serviceProvider().services().stream().map(Service::id).toList();
        }, this::screen));

    }

    @Override
    public void executeBase() {
        log.error(this.i18nProvider().get("global.wrongUsage"));
        log.info("service [shutdown] [name]");
        log.info("service [screen] [name]");
    }

    private void shutdown(String[] args) {
        if(args.length < 2) {
            this.executeBase();
            return;
        }
        var service = EasyCloudClusterOld.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error(i18nProvider().get("command.service.notFound"));
            return;
        }

        ((ServiceImpl) service).shutdown();
    }

    private void screen(String[] args) {
        if(args.length < 2) {
            this.executeBase();
            return;
        }
        var service = (ServiceImpl) EasyCloudClusterOld.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error(i18nProvider().get("command.service.notFound"));
            return;
        }

        ((TerminalCompleter) EasyCloudClusterOld.instance().terminal().lineReader().getCompleter()).enabled(false);

        EasyCloudClusterOld.instance().terminal().logging(true);
        EasyCloudClusterOld.instance().terminal().clear(false);

        service.logCache().forEach(service::print);

        service.logStream(true);
    }
}
