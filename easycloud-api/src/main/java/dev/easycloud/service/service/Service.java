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


    default <T> T property(String key, Class<T> clazz) {
        var value = String.valueOf(this.properties().get(key));
        Object result = value;
        if(clazz.getSimpleName().equalsIgnoreCase("integer")) {
            result = (int) Double.parseDouble(value);
        }
        if(clazz.getSimpleName().equalsIgnoreCase("boolean")) {
            result = Boolean.parseBoolean(value);
        }
        if(clazz.getSimpleName().equalsIgnoreCase("double")) {
            result = Double.parseDouble(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("float")) {
            result = Float.parseFloat(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("long")) {
            result = Long.parseLong(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("short")) {
            result = Short.parseShort(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("byte")) {
            result = Byte.parseByte(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("char")) {
            result = Character.valueOf(String.valueOf(value).charAt(0));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("class")) {
            result = Enum.valueOf((Class<Enum>) clazz, String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("path")) {
            result = Path.of(String.valueOf(value));
        }
        if(clazz.getSimpleName().equalsIgnoreCase("uuid")) {
            result = UUID.fromString(String.valueOf(value));
        }
        return (T) result;
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

    void publish();
}
