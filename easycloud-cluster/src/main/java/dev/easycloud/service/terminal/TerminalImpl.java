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
import org.jline.widget.TailTipWidgets;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private boolean screenPrinting = false;

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

        var tailtipWidgets = new TailTipWidgets(this.lineReader, new HashMap<>(), 5, TailTipWidgets.TipType.COMPLETER);
        tailtipWidgets.enable();

        System.setOut(new SimpleLoggingStream(this::print).printStream());
        System.setErr(new SimpleLoggingStream(result -> this.print(ansi().fgRgb(LogType.ERROR.rgb()).a(result).reset().toString())).printStream());


        @SuppressWarnings("deprecation")
        var url = new URL("https://api.github.com/repos/EasyCloudService/cloud/contributors");
        try {
            var mapper = new ObjectMapper().readTree(url);
            mapper.forEach(node -> {
                if(node.get("login") == null || node.get("type").asText().equalsIgnoreCase("bot")) {
                    return;
                }
                if(node.get("contributions").asInt() < 15) {
                    return;
                }

                this.contributors.add(node.get("login").asText());
            });
            if(this.contributors.size() == 1) this.contributors.add("EasyCloud");
        } catch (Exception exception) {
            this.contributors.add("FlxwDNS");
            this.contributors.add("EasyCloud\n\n       Unable to load contributors,\n   please check your internet connection.");
        }
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

    public void exitScreen(ServiceImpl service) {
        EasyCloudCluster.instance().terminal().screenPrinting(false);

        if (service != null) {
            service.logStream(false);
        }

        TerminalCompleter.enabled(true);
        EasyCloudCluster.instance().terminal().revert();
    }

    private final List<String> contributors = new ArrayList<>();
    public void redraw() {
        var contributorsString = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            contributorsString.append(ansi().fgRgb(LogType.PRIMARY.rgb()).a(this.contributors.get(i)).reset());
            contributorsString.append(", ");
        }
        contributorsString.append(" \n");

        int length = 0;
        var tmpString = new StringBuilder();
        for (int i = 0; i < this.contributors.size() - 2; i++) {
            var name = this.contributors.get(i + 2);
            length = length + name.length();
            tmpString.append(ansi().fgRgb(LogType.PRIMARY.rgb()).a(name).reset()).append(", ");

            if ((i > 0 && i % 2 == 0) || this.contributors.size() == i + 3) {
                var space = Math.max(28 - length, 7);
                contributorsString.append(" ".repeat(space)).append(tmpString).append("\n");
                tmpString = new StringBuilder();
                length = 0;
            }
        }

        var layout = ansi()
                .fgRgb(LogType.PRIMARY.rgb()).a("       _           ").reset().a(" _              \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("      |_  _.  _    ").reset().a("/  |  _       _| \n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("      |_ (_| _> \\/ ").reset().a("\\_ | (_) |_| (_|\n").reset()
                .fgRgb(LogType.PRIMARY.rgb()).a("                /  ").reset().a("\n").reset()

                .reset().a("     " + EasyCloudCluster.instance().i18nProvider().get("global.contributors") + ": ")
                .reset().a(contributorsString)
                .reset().a("\n").toString();

        for (String s : layout.split("\n")) {
            this.terminal.writer().println("    " + s);
        }
        this.update();
    }
}
