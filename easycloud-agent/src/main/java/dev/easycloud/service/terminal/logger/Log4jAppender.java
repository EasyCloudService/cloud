package dev.easycloud.service.terminal.logger;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.terminal.LogType;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Log4j2Appender. Thanks to: https://github.com/HttpMarco/polocloud
 */

@Plugin(name = "Log4jAppender", category = Core.CATEGORY_NAME, elementType = "appender", printObject = true)
public final class Log4jAppender extends AbstractAppender {
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public Log4jAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @PluginBuilderFactory
    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends AbstractAppender.Builder<Builder> implements org.apache.logging.log4j.core.util.Builder<Log4jAppender> {

        @Override
        public Log4jAppender build() {
            return new Log4jAppender(getName(), getFilter(), getLayout(), isIgnoreExceptions(), getPropertyArray());
        }
    }

    @Override
    public void append(@NotNull LogEvent event) {
        var message = event.getMessage().getFormattedMessage();
        if(event.getLevel().name().equals("ERROR")) {
            message = ansi().fgRgb(LogType.ERROR.rgb()).a(message).reset().toString();
        }

        var PATTERN = ansi()
                .fgRgb(LogType.GRAY.rgb()).a("[")
                .fgRgb(LogType.WHITE.rgb()).a(DATE_FORMAT.format(Calendar.getInstance().getTime()))
                .fgRgb(LogType.GRAY.rgb()).a("]")
                .fgRgb(LogType.PRIMARY.rgb()).a(" " + event.getLevel().name() + ": ")
                .reset().a(format(message) + "\r");

        TerminalCompleter.TEMP_VALUES().clear();

        if(event.getMessage().getFormattedMessage().startsWith("SERVICE_LOG: ")) {
            System.out.println(PATTERN.toString().replace("SERVICE_LOG: ", ""));
        }

        if(!EasyCloudAgent.instance().terminal().screenPrinting()) {
            System.out.println(PATTERN.toString());
            EasyCloudAgent.instance().terminal().history().add(PATTERN.toString());
        }
    }

    private String format(String message) {
        return message
                .replace("ready", ansi().fgRgb(LogType.SUCCESS.rgb()).a("ready").reset().toString())
                .replace("success", ansi().fgRgb(LogType.SUCCESS.rgb()).a("success").reset().toString())
                .replace("canceled", ansi().fgRgb(LogType.ERROR.rgb()).a("canceled").reset().toString())
                .replace("completed", ansi().fgRgb(LogType.SUCCESS.rgb()).a("completed").reset().toString())
                .replace("error", ansi().fgRgb(LogType.ERROR.rgb()).a("error").reset().toString())
                .replace("failed", ansi().fgRgb(LogType.ERROR.rgb()).a("failed").reset().toString());
    }
}
