package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.SubCommand;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.terminal.logger.LogType;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import lombok.extern.log4j.Log4j2;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class ServiceCommand extends Command {
    public ServiceCommand() {
        super("service", "command.service.info", "ser");

        addSubCommand(new SubCommand("shutdown", "command.service.shutdown.info", this::shutdown));
        addSubCommand(new SubCommand("screen", "command.service.screen.info", this::screen));

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
        var service = EasyCloudAgent.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error(i18nProvider().get("command.service.notFound"));
            return;
        }

        ((SimpleService) service).shutdown();
    }

    private void screen(String[] args) {
        if(args.length < 2) {
            this.executeBase();
            return;
        }
        var service = (SimpleService) EasyCloudAgent.instance().serviceProvider().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error(i18nProvider().get("command.service.notFound"));
            return;
        }

        TerminalCompleter.enabled(false);
        TerminalCompleter.TEMP_VALUES().clear();

        EasyCloudAgent.instance().terminal().screenPrinting(true);
        EasyCloudAgent.instance().terminal().clear();

        service.logCache().forEach(service::print);

        service.logStream(true);
        log.info("SERVICE_LOG: " + this.i18nProvider().get("command.service.screenOpen", ansi().fgRgb(LogType.ERROR.rgb()).a("exit").reset()));
    }
}
