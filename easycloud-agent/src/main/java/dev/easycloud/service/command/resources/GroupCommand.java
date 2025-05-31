package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class GroupCommand extends Command {
    public GroupCommand() {
        super("group", "command.group.info", "g");

        addSubCommand(new SubCommand("list", "command.group.list.info", this::list));
        addSubCommand(new SubCommand("setup", "command.group.setup.info", this::setup));
        addSubCommand(new SubCommand("launch", "command.group.launch.info", this::launch));
    }

    @Override
    public void executeBase() {
        log.error("Wrong usage.");
        log.info("group [list]");
        log.info("group [setup]");
        log.info("group [delete] [name]");
        log.info("group [launch]");
    }

    private void list(String[] args) {
        var groups = EasyCloudAgent.instance().groupProvider().groups();
        if(groups.isEmpty()) {
            log.error(this.i18nProvider().get("command.group.list.noFound"));
            return;
        }

        groups.forEach(it -> {
            log.info("");
            log.info(ansi().fgRgb(LogType.PRIMARY.rgb()).a(it.name()).reset());
            log.info("* Memory: {}", it.properties().memory() + "mb");
            log.info("* Platform: {}", it.platform().initializerId() + "_" + it.platform().version());
        });
        log.info("");
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", this.i18nProvider().get("command.group.setup.name"), null))
                .add(new SetupData<>("platform", this.i18nProvider().get("command.group.setup.platform"), EasyCloudAgent.instance().platformProvider().platforms().stream().map(it -> it.initializerId() + "-" + it.version()).toList()))
                .add(new SetupData<>("memory", this.i18nProvider().get("command.group.setup.memory"), null))
                .add(new SetupData<>("maxPlayers", this.i18nProvider().get("command.group.setup.maxPlayers"), null))
                .add(new SetupData<>("always", this.i18nProvider().get("command.group.setup.always"), null))
                .add(new SetupData<>("maximum", this.i18nProvider().get("command.group.setup.maximum"), null))
                .add(new SetupData<>("static", this.i18nProvider().get("command.group.setup.static"), List.of("true", "false")))
                .publish()
                .thenAccept(it -> {
                    var memory = it.result("memory", Integer.class);
                    if(memory < 512) {
                        log.error(this.i18nProvider().get("command.group.setup.memory.invalid"));
                        return;
                    }
                    var maxPlayers = it.result("maxPlayers", Integer.class);
                    if(maxPlayers < 1) {
                        log.error(this.i18nProvider().get("command.group.setup.maxPlayers.invalid"));
                        return;
                    }
                    var always = it.result("always", Integer.class);
                    if(always < 0) {
                        log.error(this.i18nProvider().get("command.group.setup.always.invalid"));
                        return;
                    }

                    var group = new Group(
                            false,
                            it.result("name", String.class),
                            EasyCloudAgent.instance().platformProvider().platforms().stream().filter(platform -> (platform.initializerId() + "-" + platform.version()).equals(it.result("platform", String.class))).findFirst().orElseThrow(),
                            new GroupProperties(
                                    it.result("memory", Integer.class),
                                    it.result("maxPlayers", Integer.class),
                                    it.result("always", Integer.class),
                                    it.result("maximum", Integer.class),
                                    Boolean.parseBoolean(it.result("static", String.class))
                            ));

                    EasyCloudAgent.instance().groupProvider().create(group);
                });
    }

    private void launch(String[] args) {
        SetupService.simple()
                .add(new SetupData<>("group", this.i18nProvider().get("command.group.launch.group"), EasyCloudAgent.instance().groupProvider().groups().stream().map(Group::name).toList()))
                .add(new SetupData<>("amount", this.i18nProvider().get("command.group.launch.amount"), null))
                .publish()
                .thenAccept(it -> {
                    var group = EasyCloudAgent.instance().groupProvider().get(it.result("group", String.class));
                    if(group == null) {
                        log.error(this.i18nProvider().get("command.group.notFound"));
                        return;
                    }

                    var amount = it.result("amount", Integer.class);
                    log.info(this.i18nProvider().get("command.group.launch.success", ansi().fgRgb(LogType.WHITE.rgb()).a(group.name()).reset(), amount));
                    EasyCloudAgent.instance().serviceProvider().launch(group, amount);
                });
    }
}
