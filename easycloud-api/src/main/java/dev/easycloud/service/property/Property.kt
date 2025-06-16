package dev.easycloud.service.property;

import lombok.Getter;

@Getter
public final class Property<T> {
    private final String key;
    private final Class<T> type;
    private final String className;

    public Property(final String key, final Class<T> type) {
        this.key = key;
        this.type = type;
        this.className = type.getSimpleName().toLowerCase();
    }
}
