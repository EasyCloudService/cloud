package dev.easycloud.service.group.command;

import dev.easycloud.service.group.GroupProvider;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.CommandNode;
import dev.easycloud.service.i18n.I18nProvider;
import dev.easycloud.service.platform.PlatformProvider;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.ServiceProvider;
import dev.easycloud.service.service.launch.ServiceLaunchBuilder;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.logger.Log4jColor;
import io.activej.inject.annotation.Inject;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class GroupCommand extends Command {
    private final I18nProvider i18nProvider;
    private final GroupProvider groupProvider;
    private final ServiceProvider serviceProvider;
    private final PlatformProvider platformProvider;

    @Inject
    public GroupCommand(
            I18nProvider i18nProvider, GroupProvider groupProvider,
            ServiceProvider serviceProvider, PlatformProvider platformProvider
    ) {
        super("group", i18nProvider.get("command.group.info"));
        this.i18nProvider = i18nProvider;
        this.groupProvider = groupProvider;
        this.serviceProvider = serviceProvider;
        this.platformProvider = platformProvider;

        addSubCommand(new CommandNode("list", i18nProvider.get("command.group.list.info"), this::list));
        addSubCommand(new CommandNode("setup", i18nProvider.get("command.group.setup.info"), this::setup));
        addSubCommand(new CommandNode("delete", i18nProvider.get("command.group.delete.info"), unused -> this.groupProvider.groups()
                .stream().map(Group::getName)
                .toList(), this::delete)
        );
        addSubCommand(new CommandNode("launch", i18nProvider.get("command.group.launch.info"), unused -> this.groupProvider.groups()
                .stream()
                .map(Group::getName)
                .toList(), this::launch)
        );
    }

    @Override
    public void executeBase() {
        log.error("Wrong usage.");
        log.info("group [list]");
        log.info("group [setup]");
        log.info("group [delete] [name]");
        log.info("group [launch] [name] [amount]");
    }

    private void list(String[] args) {
        var groups = this.groupProvider.groups();
        if (groups.isEmpty()) {
            log.error(this.i18nProvider.get("command.group.list.noFound"));
            return;
        }

        groups.forEach(it -> {
            log.info("");
            log.info(ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a(it.getName()).reset());
            log.info("* Memory: {}", it.read(GroupProperties.MEMORY()) + "mb");
            log.info("* Platform: {}", it.getPlatform().initializerId() + "_" + it.getPlatform().version());
        });
        log.info("");
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", this.i18nProvider.get("command.group.setup.name"), null))
                .add(new SetupData<>("platform", this.i18nProvider.get("command.group.setup.platform"), this.platformProvider.platforms().stream().map(it -> it.initializerId() + "-" + it.version()).toList()))
                .add(new SetupData<>("memory", this.i18nProvider.get("command.group.setup.memory"), null))
                .add(new SetupData<>("maxPlayers", this.i18nProvider.get("command.group.setup.maxPlayers"), null))
                .add(new SetupData<>("always", this.i18nProvider.get("command.group.setup.always"), null))
                .add(new SetupData<>("maximum", this.i18nProvider.get("command.group.setup.maximum"), null))
                .add(new SetupData<>("static", this.i18nProvider.get("command.group.setup.static"), List.of("yes", "no")))
                .add(new SetupData<>("continue", this.i18nProvider.get("global.setup.continue"), List.of("yes", "no")))
                .publish()
                .thenAccept(it -> {
                    if (!it.result("continue", String.class).equalsIgnoreCase("yes")) {
                        log.info(this.i18nProvider.get("global.setup.cancel"));
                        return;
                    }

                    var memory = it.result("memory", Integer.class);
                    if (memory < 512) {
                        log.error(this.i18nProvider.get("command.group.setup.memory.invalid"));
                        return;
                    }

                    var maxPlayers = it.result("maxPlayers", Integer.class);
                    if (maxPlayers < 1) {
                        log.error(this.i18nProvider.get("command.group.setup.maxPlayers.invalid"));
                        return;
                    }

                    var always = it.result("always", Integer.class);
                    if (always < 0) {
                        log.error(this.i18nProvider.get("command.group.setup.always.invalid"));
                        return;
                    }

                    var maximum = it.result("maximum", Integer.class);
                    if (maximum < 1 && maximum != -1) {
                        log.error(this.i18nProvider.get("command.group.setup.maximum.invalid"));
                        return;
                    }


                    var platform = this.platformProvider.platforms().stream()
                            .filter(it2 -> (it2.initializerId() + "-" + it2.version()).equals(it.result("platform", String.class)))
                            .findFirst()
                            .orElse(null);
                    if (platform == null) {
                        log.error(this.i18nProvider.get("command.group.setup.platform.invalid"));
                        return;
                    }

                    var group = new Group(false, it.result("name", String.class), platform);
                    group.insert(GroupProperties.MEMORY(), memory);
                    group.insert(GroupProperties.MAX_PLAYERS(), maxPlayers);
                    group.insert(GroupProperties.ALWAYS_RUNNING(), always);
                    group.insert(GroupProperties.MAXIMUM_RUNNING(), maximum);
                    group.insert(GroupProperties.SAVE_FILES(), it.result("static", String.class).equalsIgnoreCase("yes"));
                    group.insert(GroupProperties.PRIORITY(), platform.type().equals(PlatformType.PROXY) ? 1 : 0);
                    group.insert(GroupProperties.DYNAMIC_SIZE(), true);

                    this.groupProvider.create(group);
                    log.info(this.i18nProvider.get("command.group.setup.success"));
                });
    }

    private void delete(String[] args) {
        if (args.length < 2) {
            this.executeBase();
            return;
        }

        var groupName = args[1];
        var group = this.groupProvider.get(groupName);
        if (group == null) {
            log.error(this.i18nProvider.get("command.group.notFound"));
            return;
        }

        var services = this.serviceProvider.services()
                .stream()
                .filter(it -> it.group().getName().equals(groupName))
                .toList();
        for (Service service : services) {
            this.serviceProvider.shutdown(service);
        }

        this.groupProvider.delete(group);
        log.info(this.i18nProvider.get("command.group.delete.success", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(group.getName()).reset()));
    }


    private void launch(String[] args) {
        if (args.length != 3) {
            this.executeBase();
            return;
        }

        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            this.executeBase();
            return;
        }

        var groupName = args[1];
        if (this.groupProvider.get(groupName) == null) {
            log.error(this.i18nProvider.get("command.group.launch.wrong.group"));
            return;
        }
        this.serviceProvider.launch(new ServiceLaunchBuilder(groupName), Integer.parseInt(args[2]));
        log.info(this.i18nProvider.get("command.group.launch.success", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(groupName).reset(), Integer.parseInt(args[2])));
    }
}
