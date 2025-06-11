package dev.easycloud.service.terminal;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.service.ServiceImpl;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
@Getter
@Accessors(fluent = true)
@SuppressWarnings("CallToPrintStackTrace")
public final class TerminalImpl {
    private final String prompt;

    private final Terminal terminal;
    private final LineReaderImpl lineReader;
    private final List<String> history = new ArrayList<>();

    private TerminalReadingThread readingThread;

    @Setter
    private boolean logging = false;

    @SneakyThrows
    public TerminalImpl() {
        this.prompt = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("cluster")
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
                EasyCloudCluster.instance().shutdown();
                return;
            }
            exception.printStackTrace();
            EasyCloudCluster.instance().shutdown();
        });
        this.readingThread.start();
        this.clear();
    }

    public void clear() {
        this.clear(true);
    }

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

    public void exit(ServiceImpl service) {
        EasyCloudCluster.instance().terminal().logging(false);

        if (service != null) {
            service.logStream(false);
        }

        ((TerminalCompleter) this.lineReader.getCompleter()).enabled(true);
        EasyCloudCluster.instance().terminal().revert();
    }

    public void redraw() {
        var layout = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("           _           ").reset().a(" _              \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("          |_  _.  _    ").reset().a("/  |  _       _| \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("          |_ (_| _> \\/ ").reset().a("\\_ | (_) |_| (_|\n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("                    /  ").reset().a("\n").reset()
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println(s);
        }
        this.update();
    }
}
