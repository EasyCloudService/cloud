package dev.easycloud.service.service.launch;

import dev.easycloud.service.property.Property;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Accessors(chain = true)
@SuppressWarnings("unchecked")
public final class ServiceLaunchBuilder {
    private final UUID builderId = UUID.randomUUID();
    private final String group;
    @Setter
    private int id;

    private final Map<String, Object> properties;

    public ServiceLaunchBuilder(String group) {
        this.group = group;
        this.id = 0; // If its under 1, it will be set by the service provider
        this.properties = new HashMap<>();
    }

    public <T> ServiceLaunchBuilder override(Property<T> property, T value) {
        this.properties.put(property.getKey(), value);
        return this;
    }

    public <T> T property(Property<T> property, T defaultValue) {
        return (T) this.properties.getOrDefault(property.getKey(), defaultValue);
    }
}
