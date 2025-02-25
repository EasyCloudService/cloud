package dev.easycloud.service.terminal;


import dev.easycloud.service.EasyCloudAgent;
import lombok.Getter;
import org.jline.reader.impl.LineReaderImpl;

import java.util.function.Consumer;

public class TerminalReadingThread extends Thread {
    private final SimpleTerminal terminal;
    private final LineReaderImpl lineReader;

    public TerminalReadingThread(SimpleTerminal terminal) {
        super("Console-Thread");

        this.terminal = terminal;
        this.lineReader = this.terminal.lineReader();
    }

    @Getter
    private Consumer<String> prioSub = null;
    public void prioSub(Consumer<String> consumer) {
        this.prioSub = consumer;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            var line = this.lineReader.readLine(this.terminal.prompt());
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