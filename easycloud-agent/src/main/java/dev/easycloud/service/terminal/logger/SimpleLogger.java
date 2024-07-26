package dev.easycloud.service.terminal.logger;

import dev.easycloud.service.EasyCloudAgent;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.log4j.*;

import static org.fusesource.jansi.Ansi.*;

@Accessors(fluent = true)
public final class SimpleLogger {
    @Getter
    private static final Logger logger = LogManager.getLogger(EasyCloudAgent.class.getName());

    static {
        var console = new ConsoleAppender();
        var PATTERN = ansi()
                .fgRgb(LoggerColor.GRAY.rgb()).a("[")
                .fgRgb(LoggerColor.WHITE.rgb()).a("%d{HH:mm:ss}")
                .fgRgb(LoggerColor.GRAY.rgb()).a("]")
                .fgRgb(LoggerColor.PRIMARY.rgb()).a(" %p: ")
                .reset().a("%m\n");

        console.setLayout(new PatternLayout(PATTERN.toString()));
        console.setThreshold(Level.ALL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
    }

    public static void info(String line) {
        logger.info(line);
    }

    public static void warning(String line) {
        logger.warn(line);
    }

    public static void error(String line) {
        logger.error(line);
    }
}
