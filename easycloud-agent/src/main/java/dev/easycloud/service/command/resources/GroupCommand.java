package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupData;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class GroupCommand extends Command {
    public GroupCommand() {
        super("group", "Manage groups.", "g");

        addSubCommand(new SubCommand("list", "List all groups.", this::list));
        addSubCommand(new SubCommand("setup", "Start group setup.", this::setup));
        addSubCommand(new SubCommand("launch", "Launch group amount.", this::launch));
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
        var groups = EasyCloudAgent.instance().groupHandler().groups();
        if(groups.isEmpty()) {
            log.error("No groups found.");
            return;
        }

        groups.forEach(it -> {
            log.info("");
            log.info(ansi().fgRgb(LogType.PRIMARY.rgb()).a(it.name()).reset());
            log.info("* Memory: {}", it.data().memory() + "mb");
            log.info("* Platform: {}", it.platform().initilizerId() + "_" + it.platform().version());
        });
        log.info("");
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", "What should the name be?", null))
                .add(new SetupData<>("platform", "What should the platform be?", EasyCloudAgent.instance().platformHandler().platforms().stream().map(it -> it.initilizerId() + "-" + it.version()).toList()))
                .add(new SetupData<>("memory", "How much memory should the group have?", null))
                .add(new SetupData<>("maxPlayers", "How many players should be online (max)", null))
                .add(new SetupData<>("always", "How much services should always be online?", null))
                .add(new SetupData<>("maximum", "How much services should be online maximal? (-1 = no limit)", null))
                .add(new SetupData<>("static", "Should the group be static?", List.of("true", "false")))
                .publish()
                .thenAccept(it -> {
                    var group = new Group(
                            false,
                            it.result("name", String.class),
                            EasyCloudAgent.instance().platformHandler().platforms().stream().filter(platform -> (platform.initilizerId() + "-" + platform.version()).equals(it.result("platform", String.class))).findFirst().orElseThrow(),
                            new GroupData(
                                    it.result("memory", Integer.class),
                                    it.result("maxPlayers", Integer.class),
                                    it.result("always", Integer.class),
                                    it.result("maximum", Integer.class),
                                    Boolean.parseBoolean(it.result("static", String.class))
                            ));
                    EasyCloudAgent.instance().groupHandler().create(group);
                });
    }

    private void launch(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("group", "What should the group be?", EasyCloudAgent.instance().groupHandler().groups().stream().map(Group::name).toList()))
                .add(new SetupData<>("amount", "What should the amount be?", null))
                .publish()
                .thenAccept(it -> {
                    var group = EasyCloudAgent.instance().groupHandler().get(it.result("group", String.class));
                    if(group == null) {
                        log.error("Group not found.");
                        return;
                    }

                    var amount = it.result("amount", Integer.class);
                    log.info("{} queued {} services to launch...", ansi().fgRgb(LogType.WHITE.rgb()).a(group.name()).reset(), amount);
                    EasyCloudAgent.instance().serviceHandler().launch(group, amount);
                });
    }
}
