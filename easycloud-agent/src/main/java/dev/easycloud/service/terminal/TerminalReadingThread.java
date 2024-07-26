package dev.easycloud.service.terminal;


import org.apache.log4j.Logger;
import org.jline.reader.LineReader;

public class TerminalReadingThread extends Thread {
    private final String prompt;
    private final SimpleTerminal terminal;
    private final LineReader lineReader;

    public TerminalReadingThread(Logger logger, SimpleTerminal terminal) {
        super("Console-Thread");

        this.terminal = terminal;
        this.lineReader = this.terminal.lineReader();
        this.prompt = "&7▶▷ &fEasy&9Cloud &7» &r";
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            var line = this.lineReader.readLine(this.prompt);
            if (line != null && !line.isEmpty()) {
                System.out.println(line);
            }
        }
    }

}