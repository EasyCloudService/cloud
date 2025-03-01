package dev.easycloud.service.service;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
@Getter
@Setter
public final class SimpleService implements Service {
    private final String id;
    private final Group group;

    private ServiceState state;

    private final int port;
    private final Path directory;

    private Process process;
    private boolean logStream;

    private final List<String> logCache;

    public SimpleService(String id, Group group, int port, Path directory) {
        this.id = id;
        this.group = group;
        this.port = port;
        this.directory = directory;

        this.process = null;
        this.logStream = false;

        this.logCache = new ArrayList<>();
    }

    @Override
    public void state(ServiceState serviceState) {
        this.state = serviceState;
    }

    public void process(Process process) {
        this.process = process;

        this.printStream(process.getInputStream());
        this.printStream(process.getErrorStream());
    }

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
        log.info(EasyCloudAgent.instance().i18nProvider().get("service.shutdown", ansi().fgRgb(LogType.WHITE.rgb()).a(this.id).reset()));
        EasyCloudAgent.instance().terminal().exitScreen(this);

        new Thread(() -> {
            if (!this.group.data().isStatic()) {
                this.process.destroyForcibly();
            }

            try {
                this.process.waitFor();
                if (!this.group.data().isStatic()) {
                    FileFactory.remove(this.directory);
                }
                EasyCloudAgent.instance().serviceProvider().services().remove(this);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }).start();
    }

    public void print(String line) {
        if (line.startsWith("[")) {
            log.info("SERVICE_LOG: " + line.substring(17));
        } else {
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
                throw new RuntimeException(exception);
            }
        }).start();
    }
}