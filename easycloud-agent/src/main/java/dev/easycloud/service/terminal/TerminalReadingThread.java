package dev.easycloud.service.terminal;


import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;

import java.util.function.Consumer;

import static org.fusesource.jansi.Ansi.*;

public class TerminalReadingThread extends Thread {
    private final String prompt;
    private final SimpleTerminal terminal;
    private final LineReaderImpl lineReader;

    public TerminalReadingThread(Logger logger, SimpleTerminal terminal) {
        super("Console-Thread");

        this.terminal = terminal;
        this.lineReader = (LineReaderImpl) this.terminal.lineReader();

        this.prompt = ansi()
                .fgRgb(LoggerColor.PRIMARY.rgb()).a("easyCloud")
                .fgRgb(LoggerColor.GRAY.rgb()).a("@")
                .fgRgb(LoggerColor.WHITE.rgb()).a("agent")
                .fgRgb(LoggerColor.GRAY.rgb()).a(": ").toString();
    }

    @Getter
    private Consumer<String> prioSub = null;
    public void prioSub(Consumer<String> consumer) {
        this.prioSub = consumer;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            var line = this.lineReader.readLine(this.prompt);
            if (line != null && !line.isEmpty()) {
                if(prioSub != null) {
                    prioSub.accept(line);
                    prioSub = null;
                    continue;
                }

                if(!line.contains(" ")) {
                    EasyCloudAgent.instance().commandHandler().execute(line, new String[0]);
                } else {
                    var args = line.split(" ");
                    EasyCloudAgent.instance().commandHandler().execute(args[0], line.replaceFirst(args[0], "").split(" "));
                }
            }
        }
    }

}