package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
@Getter
@Setter
public final class ServiceImpl implements Service {
    private final String id;
    private final Group group;

    private ServiceState state;

    private final String directoryRaw;

    private final Map<String, Object> properties = new HashMap<>();

    // transient
    private transient Process process;
    private transient boolean logStream;
    private transient final List<String> logCache = new ArrayList<>();

    public ServiceImpl(String id, Group group, Path directory) {
        this.id = id;
        this.group = group;

        this.state = ServiceState.STARTING;

        this.directoryRaw = directory.toString();

        this.process = null;
        this.logStream = false;
    }

    public void state(ServiceState serviceState) {
        this.state = serviceState;
    }

    @Override
    public void publish() {
        EasyCloudCluster.instance().eventProvider().publish(new ServiceUpdateEvent(this));
    }

    public void process(Process process) {
        this.process = process;

        this.printStream(process.getInputStream());
        this.printStream(process.getErrorStream());
    }

    public void execute(String command) {
        var outputStream = process.getOutputStream();
        try {
            if (process != null && outputStream != null) {
                outputStream.write((command + "\n").getBytes());
                outputStream.flush();
                return;
            }
        } catch (Exception exception) {
            if(!this.process.isAlive()) {
                return;
            }
            log.error(EasyCloudCluster.instance().i18nProvider().get("service.stream.failed", exception));
            return;
        }
        log.error(EasyCloudCluster.instance().i18nProvider().get("service.command.failed", command));
    }

    public void shutdown() {
        this.execute("stop");
        log.info(EasyCloudCluster.instance().i18nProvider().get("service.shutdown", ansi().fgRgb(LogType.WHITE.rgb()).a(this.id).reset()));
        EasyCloudCluster.instance().terminal().exitScreen(this);

        new Thread(() -> {
            if (!this.group.property(GroupProperties.SAVE_FILES())) {
                this.process.destroyForcibly();
            }

            try {
                this.process.waitFor();
                if (!this.group.property(GroupProperties.SAVE_FILES())) {
                    FileFactory.remove(this.directory());
                }
                EasyCloudCluster.instance().serviceProvider().services().remove(this);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }).start();
    }

    @SuppressWarnings("all")
    public void print(String line) {
        if (line.startsWith("[") && line.contains(":") && line.split("]:")[0].length() == 14) {
            log.info("SERVICE_LOG: " + line.substring(17));
        } else if (line.startsWith("[") && line.contains(":") && line.split("] ")[0].length() == 14) {
            log.info("SERVICE_LOG: " + line.substring(16));
        }  else {
            log.info("SERVICE_LOG: " + line);
        }
    }

    private void printStream(InputStream inputStream) {
        new Thread(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (this.logStream) {
                        this.print(line);
                    }
                    this.logCache.add(line);
                }
            } catch (IOException exception) {
                this.print(exception.getMessage());
            }
        }).start();
    }
}