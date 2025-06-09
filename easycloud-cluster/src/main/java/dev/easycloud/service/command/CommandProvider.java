package dev.easycloud.service.command;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.command.resources.*;
import dev.easycloud.service.group.command.GroupCommand;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.command.ServiceCommand;
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

    public CommandProvider() {
        this.commands = new ArrayList<>();
        this.commands.addAll(List.of(new HelpCommand(), new ClearCommand(), new ShutdownCommand(), new GroupCommand(), new ServiceCommand(), new ReloadCommand()));
    }

    public void execute(String command, String[] args) {
        if (EasyCloudCluster.instance().terminal().screenPrinting()) {
            var service = EasyCloudCluster.instance().serviceProvider().services().stream().filter(it -> it instanceof ServiceImpl)
                    .map(it -> (ServiceImpl) it)
                    .filter(ServiceImpl::logStream)
                    .findFirst()
                    .orElse(null);

            if (service == null) {
                log.error(EasyCloudCluster.instance().i18nProvider().get("command.service.notRunning"));
            }

            if (command.equalsIgnoreCase("exit") || service == null) {
                EasyCloudCluster.instance().terminal().exitScreen(service);
                return;
            }

            service.execute(command + " " + String.join(" ", args));
            return;
        }

        this.commands.stream()
                .filter(it -> it.name().equals(command) || it.aliases().stream().anyMatch(it2 -> it2.equalsIgnoreCase(command)))
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
                }, () -> log.error(EasyCloudCluster.instance().i18nProvider().get("command.unknown", command)));
    }
}
