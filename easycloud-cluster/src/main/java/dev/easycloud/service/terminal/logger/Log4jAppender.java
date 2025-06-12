package dev.easycloud.service.terminal.logger;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.setup.SetupService;
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
            return new Log4jAppender(getName(), getFilter(), getLayout(), false, getPropertyArray());
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

        if(message.startsWith("Listening on [/")) {
            return;
        }

        if(!event.getLevel().name().equals("ERROR")) {
            if (event.getMessage().getFormattedMessage().startsWith("SETUP: ")) {
                System.out.println(PATTERN.toString().replace("SETUP: ", ""));
                return;
            }

            if (event.getMessage().getFormattedMessage().startsWith("SERVICE_LOG: ")) {
                var rawMessage = event.getMessage().getFormattedMessage();
                var logType = "INFO";
                if(rawMessage.contains("WARN")) logType = "WARN";
                if(rawMessage.contains("ERROR")) logType = "ERROR";

                System.out.println(ansi()
                        .fgRgb(LogType.GRAY.rgb()).a("[")
                        .fgRgb(LogType.WHITE.rgb()).a(DATE_FORMAT.format(Calendar.getInstance().getTime()))
                        .fgRgb(LogType.GRAY.rgb()).a("]")
                        .fgRgb(LogType.PRIMARY.rgb()).a(" " + logType + ": ")
                        .reset().a(format(message) + "\r").toString().replace("SERVICE_LOG: ", "").replace(":" + logType + " : ", ""));
                return;
            }
            if (!EasyCloudCluster.instance().terminal().logging() && SetupService.running.isEmpty()) {
                System.out.println(PATTERN.toString());
            }
        } else {
            System.out.println(PATTERN.toString());
        }
        EasyCloudCluster.instance().terminal().history().add(PATTERN.toString());
    }

    private String format(String message) {
        return message
                .replaceAll("\\bsuccessfully\\b", ansi().fgRgb(LogType.SUCCESS.rgb()).a("successfully").reset().toString())
                .replaceAll("\\bready\\b", ansi().fgRgb(LogType.SUCCESS.rgb()).a("ready").reset().toString())
                .replaceAll("\\bonline\\b", ansi().fgRgb(LogType.SUCCESS.rgb()).a("online").reset().toString())
                .replaceAll("\\bshut down\\b", ansi().fgRgb(LogType.ERROR.rgb()).a("shut down").reset().toString())
                .replaceAll("\\bsuccess\\b", ansi().fgRgb(LogType.SUCCESS.rgb()).a("success").reset().toString())
                .replaceAll("\\bcanceled\\b", ansi().fgRgb(LogType.ERROR.rgb()).a("canceled").reset().toString())
                .replaceAll("\\bcompleted\\b", ansi().fgRgb(LogType.SUCCESS.rgb()).a("completed").reset().toString())
                .replaceAll("\\berror\\b", ansi().fgRgb(LogType.ERROR.rgb()).a("error").reset().toString())
                .replaceAll("\\bfailed\\b", ansi().fgRgb(LogType.ERROR.rgb()).a("failed").reset().toString());
    }
}
