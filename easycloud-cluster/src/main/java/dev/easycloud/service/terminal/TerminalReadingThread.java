package dev.easycloud.service.terminal;


import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.Getter;
import org.jline.reader.impl.LineReaderImpl;

import java.util.function.Consumer;

import static org.fusesource.jansi.Ansi.ansi;

public final class TerminalReadingThread extends Thread {
    private final ClusterTerminal terminal;
    private final LineReaderImpl lineReader;

    public TerminalReadingThread(ClusterTerminal terminal) {
        super("Console-Thread");

        this.terminal = terminal;
        this.lineReader = this.terminal.lineReader();
    }

    @Getter
    private Consumer<String> priority = null;
    public void priority(Consumer<String> consumer) {
        this.priority = consumer;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            var prompt = this.terminal.prompt();
            if(this.terminal.logging()) {
                prompt = ansi()
                        .fgRgb(Log4jColor.WHITE.rgb()).a("service")
                        .fgRgb(Log4jColor.GRAY.rgb()).a("@")
                        .fgRgb(Log4jColor.ERROR.rgb()).a("exit")
                        .fgRgb(Log4jColor.GRAY.rgb()).a(": ").toString();
            }
            var line = this.lineReader.readLine(prompt);
            if (line != null && !line.isEmpty()) {
                if(priority != null) {
                    priority.accept(line);
                    priority = null;
                    continue;
                }

                if(line.replace(" ", "").isEmpty()) {
                    continue;
                }

                if(!line.contains(" ")) {
                    EasyCloudClusterOld.instance().commandProvider().execute(line, new String[0]);
                } else {
                    var args = line.split(" ");
                    EasyCloudClusterOld.instance().commandProvider().execute(args[0], line.replaceFirst(args[0], "").split(" "));
                }
            }
        }
    }

}