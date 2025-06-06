package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.property.Property;
import dev.easycloud.service.service.resources.ServiceState;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public interface Service {
    String id();
    Group group();

    Map<String, Object> properties();
    default void addProperty(String key, Object value) {
        if(value instanceof String || value instanceof Integer || value instanceof Boolean ||
                value instanceof Double || value instanceof Float || value instanceof Long ||
                value instanceof Short || value instanceof Byte || value instanceof Character ||
                value instanceof Class || value instanceof Enum || value instanceof Path || value instanceof UUID
        ) {
            properties().put(key, value);
        } else {
            throw new IllegalArgumentException("Invalid property type: " + value.getClass().getSimpleName());
        }
    }

    default void addProperty(Property property, Object value) {
        this.addProperty(property.key(), value);
    }

    default <T> T property(String key, Class<T> ignoredClazz) {
        return (T) properties().get(key);
    }

    default <T> T property(Property<T> property) {
        return this.property(property.key(), property.type());
    }

    ServiceState state();
    void state(ServiceState state);

    String directoryRaw();

    default Path directory() {
        return Path.of(directoryRaw());
    }
}
