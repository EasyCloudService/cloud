package dev.easycloud.service.service.resources.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Property<T> {
    private final String key;
    private final Class<T> type;
}
