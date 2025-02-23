package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.scheduler.AdvancedScheduler;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.terminal.LogType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
@Getter
@AllArgsConstructor
@Setter
public final class SimpleService implements Service {
    private final String id;
    private final Group group;

    private final int port;
    private final Path directory;

    private Process process;

    @Override
    public void execute(String command) {
        var outputStream = process.getOutputStream();
        try {
            if (process != null && outputStream != null) {
                outputStream.write((command + "\n").getBytes());
                outputStream.flush();
                return;
            }
        } catch (Exception exception) {
            log.error("Stream is not available", exception);
            return;
        }
        log.error("Command could not be executed: {}", command);
    }

    @Override
    public void shutdown() {
        this.execute("stop");
        log.info("Service {} will be shutdown.", ansi().fgRgb(LogType.WHITE.rgb()).a(this.id).reset());
        if (this.group.data().isStatic()) {
            new AdvancedScheduler((Void) -> {
                if (!this.process.isAlive()) {
                    EasyCloudAgent.instance().serviceFactory().services().remove(this);
                    return false;
                }
                return true;
            }).run(1000);
        } else {
            this.process.destroyForcibly();
            EasyCloudAgent.instance().serviceFactory().services().remove(this);
        }
    }
}
