package dev.easycloud.service.terminal;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.LogType;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
@Getter
@Accessors(fluent = true)
public final class SimpleTerminal {
    private final String prompt;

    private final Terminal terminal;
    private final LineReaderImpl lineReader;
    private final List<String> history = new ArrayList<>();

    private TerminalReadingThread readingThread;

    @Setter
    private boolean screenPrinting = false;

    @SneakyThrows
    public SimpleTerminal() {
        this.prompt = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("agent")
                .fgRgb(LogType.GRAY.rgb()).a("@")
                .fgRgb(LogType.WHITE.rgb()).a("cloud")
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

    public void revert() {
        this.clear();
        this.history.forEach(System.out::println);
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

    public void exitScreen(SimpleService service) {
        EasyCloudAgent.instance().terminal().screenPrinting(false);

        if (service != null) {
            service.logStream(false);
        }

        TerminalCompleter.enabled(true);
        EasyCloudAgent.instance().terminal().revert();
    }

    public void redraw() {
        var layout = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("   _           ").reset().a(" _              \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("  |_  _.  _    ").reset().a("/  |  _       _| \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("  |_ (_| _> \\/ ").reset().a("\\_ | (_) |_| (_|\n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("            /  ").reset().a("\n").reset()

                .reset().a(EasyCloudAgent.instance().i18nProvider().get("global.contributors") + ": ")
                .fgRgb(LogType.PRIMARY.rgb()).a("FlxwDNS")
                .reset().a(" " + EasyCloudAgent.instance().i18nProvider().get("global.and") + " ")
                .fgRgb(LogType.PRIMARY.rgb()).a("1Chickxn")
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println("        " + s);
        }
        this.terminal.writer().println("");

        this.update();

        /*var layout = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("  ______                 ").reset().a("  _____ _                 _  \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a(" |  ____|                ").reset().a(" / ____| |               | | \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a(" | |__   __ _ ___ _   _  ").reset().a("| |    | | ___  _   _  __| | \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a(" |  __| / _` / __| | | | ").reset().a("| |    | |/ _ \\| | | |/ _` | \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a(" | |___| (_| \\__ \\ |_| | ").reset().a("| |____| | (_) | |_| | (_| | \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a(" |______\\__,_|___/\\__, | ").reset().a(" \\_____|_|\\___/ \\__,_|\\__,_| \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("                  |___/                       [DEBUG]\n").reset()

                .reset().a("    " + EasyCloudAgent.instance().i18nProvider().get("global.contributors") + ": ")
                .fgRgb(LogType.PRIMARY.rgb()).a("FlxwDNS")
                .reset().a(", ")
                .fgRgb(LogType.PRIMARY.rgb()).a("1Chickxn")
                .reset().a(" " + EasyCloudAgent.instance().i18nProvider().get("global.and") + " ")
                .fgRgb(LogType.PRIMARY.rgb()).a("Swerion")
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println(s);
        }
        this.terminal.writer().println("");

        this.update();*/
    }
}
