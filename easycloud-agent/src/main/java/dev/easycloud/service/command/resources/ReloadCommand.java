package dev.easycloud.service.command.resources;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.command.Command;
import dev.easycloud.service.group.SimpleGroupProvider;
import dev.easycloud.service.terminal.LogType;
import lombok.extern.slf4j.Slf4j;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reload the configs.", "refresh");
    }

    @Override
    public void executeBase() {
        log.info("Reloading platforms...");
        EasyCloudAgent.instance().platformProvider().refresh();
        log.info("Reloading groups...");
        ((SimpleGroupProvider) EasyCloudAgent.instance().groupProvider()).scan();
        EasyCloudAgent.instance().groupProvider().groups().forEach(group -> {
            log.info(" * Found group: {}", ansi().fgRgb(LogType.WHITE.rgb()).a(group.name()).reset());
        });

        log.info("Reloaded platforms and groups.");
    }
}
