package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.files.EasyFiles;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent;
import dev.easycloud.service.network.event.resources.ServiceUpdateEvent;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.Log4jColor;
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
import java.util.concurrent.TimeUnit;

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
    private transient List<String> logCache = new ArrayList<>();

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
        EasyCloudClusterOld.instance().eventProvider().publish(new ServiceUpdateEvent(this));
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
            if (!this.process.isAlive()) {
                return;
            }
            log.error(EasyCloudClusterOld.instance().i18nProvider().get("service.stream.failed", exception));
            return;
        }
        log.error(EasyCloudClusterOld.instance().i18nProvider().get("service.command.failed", command));
    }

    public void shutdown() {
        EasyCloudClusterOld.instance().eventProvider().publish(new ServiceShutdownEvent(this));

        this.execute("stop");
        log.info(EasyCloudClusterOld.instance().i18nProvider().get("service.shutdown", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(this.id).reset()));
        EasyCloudClusterOld.instance().terminal().exit(this);

        var thread = new Thread(() -> {
            try {
                this.process.waitFor();
                if (!this.group.read(GroupProperties.SAVE_FILES())) {
                    EasyFiles.Companion.remove(this.directory());
                }
                EasyCloudClusterOld.instance().serviceProvider().services().remove(this);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        });
        thread.setName("Shutdown1-" + this.id);
        thread.start();

        var thread2 = new Thread(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                if (this.process.isAlive()) {
                    this.process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                log.error("Failed to clear log cache after service shutdown", e);
            }
        });
        thread2.setName("Shutdown2-" + this.id);
        thread2.start();
    }

    @SuppressWarnings("all")
    public void print(String line) {
        if (line.startsWith("[") && line.contains(":") && line.split("]:")[0].length() == 14) {
            log.info("SERVICE_LOG: " + line.substring(17));
        } else if (line.startsWith("[") && line.contains(":") && line.split("] ")[0].length() == 14) {
            log.info("SERVICE_LOG: " + line.substring(16));
        } else {
            log.info("SERVICE_LOG: " + line);
        }
    }

    private void printStream(InputStream inputStream) {
        var thread = new Thread(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (this.logStream) {
                        this.print(line);
                    }
                    this.logCache.add(line);
                }
            } catch (IOException exception) {
                if (exception.getMessage().contains("Stream closed")) {
                    return; // Stream was closed, no need to log this
                }
                throw new RuntimeException(exception);
            }
        });
        thread.setName("PrintStream-" + this.id);
        thread.start();
    }
}