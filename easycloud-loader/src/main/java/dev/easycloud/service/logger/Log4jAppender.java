package dev.easycloud.service.logger;

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
        System.out.println(event.getMessage().getFormattedMessage());
    }
}
