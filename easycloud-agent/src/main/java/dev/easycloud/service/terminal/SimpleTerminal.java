package dev.easycloud.service.terminal;

import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.highlighter.TerminalHighlighter;
import dev.easycloud.service.terminal.logger.LoggerColor;
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
import org.jline.widget.AutosuggestionWidgets;
import org.jline.widget.TailTipWidgets;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

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
                //.highlighter(new TerminalHighlighter())
                .build();
        var autosuggestion = new AutosuggestionWidgets(this.lineReader);
        autosuggestion.enable();

        var tailtipWidgets = new TailTipWidgets(this.lineReader, new HashMap<>(), 5, TailTipWidgets.TipType.COMPLETER);
        tailtipWidgets.enable();

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

        SimpleLogger.info("""
                
                  ____ ____ ____ _   _ ____ _    ____ _  _ ___
                  |___ |__| [__   \\_/  |    |    |  | |  | |  \\
                  |___ |  | ___]   |   |___ |___ |__| |__| |__/
                  [%RELEASE%] Contributors: FlxwDNS and 1Chickxn
                """.replace("%RELEASE%", "PRE"));

        SimpleLogger.info(ansi().a("Type").fgRgb(LoggerColor.PRIMARY.rgb()).a(" help ").reset().a("for a list of commands.").toString());
    }
}
