package dev.easycloud.service.group.resources;

import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.property.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
@Slf4j
@Getter
@Setter
@AllArgsConstructor
public final class Group {
    private boolean enabled;

    private final String name;
    private final Platform platform;

    private final Map<String, Object> properties = new HashMap<>();

    public void addProperty(Property<?> property, Object value) {
        if(value instanceof String || value instanceof Integer || value instanceof Boolean ||
                value instanceof Double || value instanceof Float || value instanceof Long ||
                value instanceof Short || value instanceof Byte || value instanceof Character ||
                value instanceof Class || value instanceof Enum || value instanceof Path || value instanceof UUID
        ) {
            properties().put(property.key(), value);
        } else {
            throw new IllegalArgumentException("Invalid property type: " + value.getClass().getSimpleName());
        }
    }

    public <T> T property(Property<T> property) {
        var value = String.valueOf(this.properties().get(property.key()));
        Object result = value;
        if(property.className().equalsIgnoreCase("integer")) {
            result = (int) Double.parseDouble(value);
        }
        if(property.className().equalsIgnoreCase("boolean")) {
            result = Boolean.parseBoolean(value);
        }
        if(property.className().equalsIgnoreCase("double")) {
            result = Double.parseDouble(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("float")) {
            result = Float.parseFloat(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("long")) {
            result = Long.parseLong(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("short")) {
            result = Short.parseShort(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("byte")) {
            result = Byte.parseByte(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("char")) {
            result = String.valueOf(value).charAt(0);
        }
        if(property.className().equalsIgnoreCase("class")) {
            result = Enum.valueOf((Class<Enum>) property.type(), String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("path")) {
            result = Path.of(String.valueOf(value));
        }
        if(property.className().equalsIgnoreCase("uuid")) {
            result = UUID.fromString(String.valueOf(value));
        }
        return (T) result;
    }
    
}