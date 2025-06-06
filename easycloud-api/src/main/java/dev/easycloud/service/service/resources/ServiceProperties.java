package dev.easycloud.service.service.resources;

import dev.easycloud.service.property.Property;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ServiceProperties {
    @Getter
    private final Property<Integer> PORT = new Property<>("PORT", Integer.class);
}
