package dev.easycloud.service;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class CloudDriver {
    private static CloudDriver instance;

    public CloudDriver() {
        instance = this;
    }

    public static CloudDriver instance() {
        return instance;
    }
}
