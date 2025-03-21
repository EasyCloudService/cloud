package dev.easycloud.service.command;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.i18n.I18nProvider;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Getter
@Accessors(fluent = true)
public abstract class Command {
    private final I18nProvider i18nProvider = EasyCloudAgent.instance().i18nProvider();

    private final String name;
    private final String description;
    private final List<String> aliases;

    private final List<SubCommand> subCommands = new ArrayList<>();

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = this.i18nProvider.get(description);
        this.aliases = Arrays.stream(aliases).toList();
    }

    public void executeBase() {
        log.info("Command not implemented!");
    }

    public void addSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }
}
