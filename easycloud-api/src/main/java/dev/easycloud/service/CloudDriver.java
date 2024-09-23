package dev.easycloud.service;

import dev.easycloud.service.category.CategoryFactory;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class CloudDriver {
    private static CloudDriver instance;

    protected CategoryFactory categoryFactory;

    public CloudDriver() {
        instance = this;
    }

    public static CloudDriver instance() {
        return instance;
    }
}
