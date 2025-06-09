package dev.easycloud.service.service.command;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ServiceCommand extends Command {
    public ServiceCommand() {
        super("service", "command.service.info", "ser");

        addSubCommand(new CommandNode("shutdown", "command.service.shutdown.info", this::shutdown));
        addSubCommand(new CommandNode("screen", "command.service.screen.info", this::screen));

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
        var service = EasyCloudCluster.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
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
        var service = (ServiceImpl) EasyCloudCluster.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error(i18nProvider().get("command.service.notFound"));
            return;
        }

        TerminalCompleter.enabled(false);
        TerminalCompleter.TEMP_VALUES().clear();

        EasyCloudCluster.instance().terminal().screenPrinting(true);
        EasyCloudCluster.instance().terminal().clear();

        service.logCache().forEach(service::print);

        service.logStream(true);
    }
}
