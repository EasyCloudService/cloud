package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.SubCommand;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupData;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.LogType;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import lombok.extern.log4j.Log4j2;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class ServiceCommand extends Command {
    public ServiceCommand() {
        super("service", "Manage service.", "ser");

        addSubCommand(new SubCommand("shutdown", "Shutdown a service.", this::shutdown));
        addSubCommand(new SubCommand("screen", "Open a service screen.", this::screen));

    }

    @Override
    public void executeBase() {
        log.error("Wrong usage.");
        log.info("service [shutdown] [name]");
        log.info("service [screen] [name]");
    }

    private void shutdown(String[] args) {
        if(args.length < 2) {
            this.executeBase();
            return;
        }
        var service = EasyCloudAgent.instance().serviceFactory().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error("Service not found.");
            return;
        }

        service.shutdown();
    }

    private void screen(String[] args) {
        if(args.length < 2) {
            this.executeBase();
            return;
        }
        var service = (SimpleService) EasyCloudAgent.instance().serviceFactory().services().stream().filter(it -> it.id().equals(args[1])).findFirst().orElse(null);
        if(service == null) {
            log.error("Service not found.");
            return;
        }

        TerminalCompleter.enabled(false);
        TerminalCompleter.TEMP_VALUES().clear();

        EasyCloudAgent.instance().terminal().screenPrinting(true);
        EasyCloudAgent.instance().terminal().clear();

        service.logCache().forEach(service::print);

        service.logStream(true);
        log.info("SERVICE_LOG: Service screen opened. Use {} to close.", ansi().fgRgb(LogType.ERROR.rgb()).a("exit").reset());
    }
}
