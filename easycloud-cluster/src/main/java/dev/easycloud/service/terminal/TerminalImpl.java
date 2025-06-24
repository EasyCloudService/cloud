package dev.easycloud.service.terminal;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.Log4jColor;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
@Getter
@Accessors(fluent = true)
@SuppressWarnings("CallToPrintStackTrace")
public final class TerminalImpl implements dev.easycloud.service.terminal.Terminal {
    private final String prompt;

    private final Terminal terminal;
    private final LineReaderImpl lineReader;
    private final List<String> history = new ArrayList<>();

    private TerminalReadingThread readingThread;
    @Setter
    private TerminalState state = TerminalState.RUNNING;

    @Setter
    private boolean logging = false;

    @SneakyThrows
    public TerminalImpl() {
        this.prompt = ansi()
                .fgRgb(Log4jColor.PRIMARY.rgb()).a("cluster")
                .fgRgb(Log4jColor.GRAY.rgb()).a("@")
                .fgRgb(Log4jColor.WHITE.rgb()).a("cloud")
                .fgRgb(Log4jColor.GRAY.rgb()).a(": ").toString();

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

        System.setOut(new SimpleLoggingStream(this::print).printStream());
        System.setErr(new SimpleLoggingStream(result -> this.print(ansi().fgRgb(Log4jColor.ERROR.rgb()).a(result).reset().toString())).printStream());
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

    @Override
    public void run() {
        System.out.println("a");
        this.readingThread = new TerminalReadingThread(this);
        System.out.println("b");
        this.readingThread.setUncaughtExceptionHandler((t, exception) -> {
            if (exception instanceof UserInterruptException) {
                EasyCloudClusterOld.instance().shutdown();
                return;
            }
            exception.printStackTrace();
            EasyCloudClusterOld.instance().shutdown();
        });
        System.out.println("c");
        this.readingThread.start();
        System.out.println("d");
        this.clear();
        System.out.println("e");
    }

    @Override
    public void clear() {
        this.clear(true);
    }

    @Override
    public void clear(boolean redraw) {
        this.terminal.puts(InfoCmp.Capability.clear_screen);
        this.terminal.flush();
        if (redraw) {
            this.redraw();
        }
    }

    public void update() {
        if (this.lineReader.isReading()) {
            this.lineReader.callWidget(LineReader.REDRAW_LINE);
            this.lineReader.callWidget(LineReader.REDISPLAY);
        }
    }

    @Override
    public void exit(Service service) {
        this.logging(false);

        if (service != null) {
            ((ServiceImpl) service).logStream(false);
        }

        ((TerminalCompleter) this.lineReader.getCompleter()).enabled(true);
        this.revert();
    }

    public void redraw() {
        var layout = ansi()
                .fgRgb(Log4jColor.PRIMARY.rgb()).a("           _           ").reset().a(" _              \n").reset()
                .fgRgb(Log4jColor.PRIMARY.rgb()).a("          |_  _.  _    ").reset().a("/  |  _       _| \n").reset()
                .fgRgb(Log4jColor.PRIMARY.rgb()).a("          |_ (_| _> \\/ ").reset().a("\\_ | (_) |_| (_|\n").reset()
                .fgRgb(Log4jColor.PRIMARY.rgb()).a("                    /  ").reset().a("\n").reset()
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println(s);
        }
        this.update();
    }
}
