package dev.easycloud.service.service.resources.property;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DefaultProperty {
    @Getter
    private final Property<Integer> PORT = new Property<>("PORT", Integer.class);
}
