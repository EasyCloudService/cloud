package dev.easycloud.service.terminal;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.stream.SimpleLoggingStream;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;
import org.jline.widget.AutosuggestionWidgets;
import org.jline.widget.TailTipWidgets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
@Getter
@Accessors(fluent = true)
public final class SimpleTerminal {
    private final String prompt;

    private final Terminal terminal;
    private final LineReaderImpl lineReader;

    private TerminalReadingThread readingThread;

    @Setter
    private boolean screenPrinting = false;

    @SneakyThrows
    public SimpleTerminal() {
        this.prompt = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("easyCloud")
                .fgRgb(LogType.GRAY.rgb()).a("@")
                .fgRgb(LogType.WHITE.rgb()).a("agent")
                .fgRgb(LogType.GRAY.rgb()).a(": ").toString();

        AnsiConsole.systemInstall();
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .streams(System.in, System.out)
                .encoding(StandardCharsets.UTF_8)
                .dumb(true)
                .jansi(true)
                .build();

        this.lineReader = new LineReaderImpl(this.terminal) {
            @Override
            protected void cleanup() {
                this.buf.clear();
                this.post = null;
                this.prompt = new AttributedString("");

                redisplay(true);

                this.terminal.puts(InfoCmp.Capability.keypad_local);
                this.terminal.trackMouse(Terminal.MouseTracking.Off);

                if (isSet(Option.BRACKETED_PASTE)) {
                    this.terminal.writer().write(BRACKETED_PASTE_OFF);
                }

                this.flush();
                this.history.moveToEnd();
            }
        };
        this.lineReader.setPrompt("");
        this.lineReader.setCompleter(new TerminalCompleter());

        var autoSuggestion = new AutosuggestionWidgets(this.lineReader);
        autoSuggestion.enable();

        var tailtipWidgets = new TailTipWidgets(this.lineReader, new HashMap<>(), 5, TailTipWidgets.TipType.COMPLETER);
        tailtipWidgets.enable();

        System.setOut(new SimpleLoggingStream(this::print).printStream());
        System.setErr(new SimpleLoggingStream(result -> this.print(ansi().fgRgb(LogType.ERROR.rgb()).a(result).reset().toString())).printStream());
    }

    private void print(String message) {
        this.terminal.puts(InfoCmp.Capability.carriage_return);
        this.terminal.writer().println(message);
        this.terminal.flush();
        this.update();
    }

    public void start() {
        this.readingThread = new TerminalReadingThread(this);
        this.readingThread.setUncaughtExceptionHandler((t, exception) -> {
            if (exception instanceof UserInterruptException) {
                EasyCloudAgent.instance().shutdown();
                return;
            }
            exception.printStackTrace();
            EasyCloudAgent.instance().shutdown();
        });
        this.readingThread.start();
    }

    public void clear() {
        this.terminal.puts(InfoCmp.Capability.clear_screen);
        this.terminal.flush();
        this.redraw();
    }

    public void update() {
        if (this.lineReader.isReading()) {
            this.lineReader.callWidget(LineReader.REDRAW_LINE);
            this.lineReader.callWidget(LineReader.REDISPLAY);
        }
    }

    public void redraw() {
        var layout = ansi().a("""
                          ____ ____ ____ _   _ ____ _    ____ _  _ ___
                          |___ |__| [__   \\_/  |    |    |  | |  | |  \\
                          |___ |  | ___]   |   |___ |___ |__| |__| |__/
                        """)
                .reset().a("  * Current version: ")
                .fgRgb(LogType.PRIMARY.rgb()).a("DEVELOPMENT")
                .reset().a("\n")
                .reset().a("  * Contributors: ")
                .fgRgb(LogType.PRIMARY.rgb()).a("FlxwDNS")
                .reset().a(" and ")
                .fgRgb(LogType.PRIMARY.rgb()).a("1Chickxn")
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println(s);
        }
        this.terminal.writer().println("");

        this.update();
    }

    public void setReadingThread(TerminalReadingThread readingThread) {
        this.readingThread = readingThread;
    }
}
