package dev.easycloud.service.terminal;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import dev.easycloud.service.terminal.stream.SimplePrintStream;
import lombok.Getter;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
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

        this.lineReader = new LineReaderImpl(this.terminal) {
            @Override
            protected void cleanup() {
                this.buf.clear();
                this.post = null;
                this.prompt = new AttributedString("");

                redisplay(false);

                this.terminal.puts(InfoCmp.Capability.keypad_local);
                this.terminal.trackMouse(Terminal.MouseTracking.Off);

                if (isSet(Option.BRACKETED_PASTE)) {
                    this.terminal.writer().write(BRACKETED_PASTE_OFF);
                }

                this.flush();
                this.history.moveToEnd();

                this.completer = new TerminalCompleter();
            }
        };

        var autoSuggestion = new AutosuggestionWidgets(this.lineReader);
        autoSuggestion.enable();

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
        this.update();

        SimpleLogger.info(ansi().a("""
                
                    ____ ____ ____ _   _ ____ _    ____ _  _ ___
                    |___ |__| [__   \\_/  |    |    |  | |  | |  \\
                    |___ |  | ___]   |   |___ |___ |__| |__| |__/
                  """)
                .reset().a("  ➥  Current version: ")
                .fgRgb(LoggerColor.PRIMARY.rgb()).a("DEVELOPMENT")
                .reset().a("\n")
                .reset().a("  ➥  Contributors: ")
                .fgRgb(LoggerColor.PRIMARY.rgb()).a("FlxwDNS")
                .reset().a(" and ")
                .fgRgb(LoggerColor.PRIMARY.rgb()).a("1Chickxn")
                .reset().a("\n").toString());

        SimpleLogger.info(ansi().a("Type").fgRgb(LoggerColor.PRIMARY.rgb()).a(" help ").reset().a("for a list of commands.").toString());
    }
}
