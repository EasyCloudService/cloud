package dev.easycloud.service.terminal;


import dev.easycloud.service.EasyCloudAgent;
import lombok.Getter;
import org.jline.reader.impl.LineReaderImpl;

import java.util.function.Consumer;

public final class TerminalReadingThread extends Thread {
    private final SimpleTerminal terminal;
    private final LineReaderImpl lineReader;

    public TerminalReadingThread(SimpleTerminal terminal) {
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
            var line = this.lineReader.readLine(this.terminal.prompt());
            if (line != null && !line.isEmpty()) {
                if(priority != null) {
                    priority.accept(line);
                    priority = null;
                    continue;
                }

                if(!line.contains(" ")) {
                    EasyCloudAgent.instance().commandProvider().execute(line, new String[0]);
                } else {
                    var args = line.split(" ");
                    EasyCloudAgent.instance().commandProvider().execute(args[0], line.replaceFirst(args[0], "").split(" "));
                }
            }
        }
    }

}