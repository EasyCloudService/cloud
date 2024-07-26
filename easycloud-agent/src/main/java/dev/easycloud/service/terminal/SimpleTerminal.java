package dev.easycloud.service.terminal;

import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.highlighter.TerminalHighlighter;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import dev.easycloud.service.terminal.stream.SimplePrintStream;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.nio.charset.StandardCharsets;

@Getter
@Accessors(fluent = true)
public final class SimpleTerminal {
    private final Terminal terminal;
    private final LineReader lineReader;

    private Thread readingThread;

    @SneakyThrows
    public SimpleTerminal() {
        AnsiConsole.systemInstall();
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .streams(System.in, System.out)
                .encoding(StandardCharsets.UTF_8)
                .dumb(true)
                .jansi(true)
                .build();

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new TerminalCompleter())
                .build();
        System.setOut(new SimplePrintStream(this.terminal.output()));

        this.start();
    }

    public void start() {
        this.clear();

        this.readingThread = new TerminalReadingThread(SimpleLogger.logger(), this);
        this.readingThread.setUncaughtExceptionHandler((t, exception) -> {
            if(exception instanceof UserInterruptException) {
                System.exit(0);
            }
            t.interrupt();
            throw new RuntimeException(exception);
        });
        this.readingThread.start();
    }

    public void clear() {
        this.terminal.puts(InfoCmp.Capability.clear_screen);
        this.terminal.flush();
        this.redraw();
    }

    public void redraw() {
        if (this.lineReader.isReading()) {
            this.lineReader.callWidget(LineReader.REDRAW_LINE);
            this.lineReader.callWidget(LineReader.REDISPLAY);
        }
    }
}
