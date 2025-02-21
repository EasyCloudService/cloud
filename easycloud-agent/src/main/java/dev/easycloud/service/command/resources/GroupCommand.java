package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupData;
import dev.easycloud.service.group.resources.GroupType;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.command.SubCommand;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

import static org.fusesource.jansi.Ansi.ansi;

@Log4j2
public final class GroupCommand extends Command {
    public GroupCommand() {
        super("group", "Manage groups.", "g");

        addSubCommand(new SubCommand("list", "List all groups.", this::list));
        addSubCommand(new SubCommand("setup", "Start group setup.", this::setup));
    }

    @Override
    public void executeBase() {
        log.error("Wrong usage.");
        log.info("group [list]");
        log.info("group [setup]");
        log.info("group [delete] [name]");
    }

    private void list(String[] args) {
        var groups = EasyCloudAgent.instance().groupFactory().groups();
        if(groups.isEmpty()) {
            log.error("No groups found.");
            return;
        }

        groups.forEach(it -> {
            log.info("");
            log.info(ansi().fgRgb(LogType.PRIMARY.rgb()).a(it.name()).reset());
            log.info("* Memory: {}", it.data().memory() + "mb");
            log.info("* Type: {}", it.type());
        });
        log.info("");
    }

    private void setup(String[] args) {
        SetupService.simple()
                .add(new SetupData<String>("name", "What should the name be?", null))
                .add(new SetupData<>("platform", "What should the platform be?", Arrays.stream(GroupType.values()).toList()))
                .add(new SetupData<>("memory", "How much memory should the group have?", null))
                .add(new SetupData<>("always", "How much services should always be online?", null))
                .add(new SetupData<>("maximum", "How much services should be online maximal? (-1 = no limit)", null))
                .publish()
                .thenAccept(it -> {
                    var group = new Group(
                            it.result("name", String.class),
                            GroupType.SERVER,
                            new GroupData(
                                    it.result("memory", Integer.class),
                                    it.result("always", Integer.class),
                                    it.result("maximum", Integer.class)
                            ));
                    EasyCloudAgent.instance().groupFactory().create(group);

                    log.info("Group created with name: {}", group.name());
                });
    }
}
