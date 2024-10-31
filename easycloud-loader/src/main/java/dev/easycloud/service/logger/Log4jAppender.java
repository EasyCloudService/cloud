package dev.easycloud.service.logger;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Plugin(name = "Log4jAppender", category = Core.CATEGORY_NAME, elementType = "appender", printObject = true)
public final class Log4jAppender extends AbstractAppender {
    public Log4jAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(@NotNull LogEvent event) {
    }
}
