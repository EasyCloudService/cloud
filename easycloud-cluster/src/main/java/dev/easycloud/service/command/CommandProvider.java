package dev.easycloud.service.command;

import dev.easycloud.service.command.resources.*;
import dev.easycloud.service.group.command.GroupCommand;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.service.command.ServiceCommand;
import dev.easycloud.service.terminal.ClusterTerminal;
import io.activej.inject.Injector;
import io.activej.inject.annotation.Inject;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Getter
@Accessors(fluent = true)
public final class CommandProvider {
    private final List<Command> commands;

    private final ClusterTerminal terminal;
    private final I18nProvider i18nProvider;
    private final ServiceProvider serviceProvider;

    @Inject
    public CommandProvider(ClusterTerminal terminal, I18nProvider i18nProvider, ServiceProvider serviceProvider) {
        this.terminal = terminal;
        this.i18nProvider = i18nProvider;
        this.serviceProvider = serviceProvider;

        this.commands = new ArrayList<>();
    }

    public void init(Injector injector) {
        this.commands.add(injector.getInstance(HelpCommand.class));
        this.commands.add(injector.getInstance(ClearCommand.class));
        this.commands.add(injector.getInstance(ShutdownCommand.class));
        this.commands.add(injector.getInstance(GroupCommand.class));
        this.commands.add(injector.getInstance(ServiceCommand.class));
        this.commands.add(injector.getInstance(ReloadCommand.class));
        this.commands.add(injector.getInstance(LocalCommand.class));
    }

    public void execute(String command, String[] args) {
        if (this.terminal.logging()) {
            var service = this.serviceProvider.services().stream().filter(it -> it instanceof ServiceImpl)
                    .map(it -> (ServiceImpl) it)
                    .filter(ServiceImpl::logStream)
                    .findFirst()
                    .orElse(null);

            if (service == null) {
                log.error(this.i18nProvider.get("command.service.notRunning"));
            }

            if (command.equalsIgnoreCase("exit") || service == null) {
               this.terminal.exit(service);
                return;
            }

            service.execute(command + " " + String.join(" ", args));
            return;
        }

        this.commands.stream()
                .filter(it -> it.name().equalsIgnoreCase(command))
                .findFirst()
                .ifPresentOrElse(it -> {
                    if (args.length == 0 || it.commandNodes().stream().noneMatch(it2 -> it2.name().equalsIgnoreCase(args[1]))) {
                        it.executeBase();
                    } else {
                        it.commandNodes().stream().filter(it2 -> it2.name().equalsIgnoreCase(args[1]))
                                .findFirst()
                                .orElseThrow()
                                .onExecute()
                                .accept(Arrays.copyOfRange(args, 1, args.length));
                    }
                }, () -> log.error(this.i18nProvider.get("command.unknown", command)));
    }
}
