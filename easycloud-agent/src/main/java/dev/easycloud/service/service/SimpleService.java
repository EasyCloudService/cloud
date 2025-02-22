package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.scheduler.AdvancedScheduler;
import dev.easycloud.service.service.resources.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

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
                outputStream.write(command.getBytes());
                outputStream.flush();
                return;
            }
        } catch (Exception e) {
            log.error("Stream is not available", e);
            log.error("Service will be shutdown...");
            this.shutdown();
            return;
        }
        log.error("Command could not be executed: {}", command);
    }

    @Override
    public void shutdown() {
        this.execute("stop");
        if(this.group.data().isStatic()) {
            new AdvancedScheduler((Void) -> {
                if (!this.process.isAlive()) {
                    log.info("Service {} has been shutdown", this.id);
                    EasyCloudAgent.instance().serviceFactory().services().remove(this);
                    return false;
                }
                return true;
            }).run(1000);
        } else {
            this.process.destroyForcibly();
            log.info("Service {} has been shutdown", this.id);
            EasyCloudAgent.instance().serviceFactory().services().remove(this);
        }
    }
}
