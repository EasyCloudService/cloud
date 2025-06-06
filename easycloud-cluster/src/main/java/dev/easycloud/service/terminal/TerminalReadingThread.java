package dev.easycloud.service.terminal;


import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.Getter;
import org.jline.reader.impl.LineReaderImpl;

import java.util.function.Consumer;

import static org.fusesource.jansi.Ansi.ansi;

public final class TerminalReadingThread extends Thread {
    private final TerminalImpl terminal;
    private final LineReaderImpl lineReader;

    public TerminalReadingThread(TerminalImpl terminal) {
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
            if(this.terminal.screenPrinting()) {
                prompt = ansi()
                        .fgRgb(LogType.WHITE.rgb()).a("service")
                        .fgRgb(LogType.GRAY.rgb()).a("@")
                        .fgRgb(LogType.ERROR.rgb()).a("exit")
                        .fgRgb(LogType.GRAY.rgb()).a(": ").toString();
            }
            var line = this.lineReader.readLine(prompt);
            if (line != null && !line.isEmpty()) {
                if(priority != null) {
                    priority.accept(line);
                    priority = null;
                    continue;
                }

                if(!line.contains(" ")) {
                    EasyCloudCluster.instance().commandProvider().execute(line, new String[0]);
                } else {
                    var args = line.split(" ");
                    EasyCloudCluster.instance().commandProvider().execute(args[0], line.replaceFirst(args[0], "").split(" "));
                }
            }
        }
    }

}