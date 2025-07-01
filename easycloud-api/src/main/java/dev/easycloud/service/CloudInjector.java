package dev.easycloud.service;

import io.activej.inject.Injector;

public class CloudInjector {
    private static Injector injector;

    public static void initialize(Injector injectorInstance) {
        if (injector != null) {
            throw new IllegalStateException("Injector is already initialized.");
        }
        injector = injectorInstance;
    }

    public static Injector get() {
        if (injector == null) {
            throw new IllegalStateException("Injector is not initialized. Please call initialize() first.");
        }
        return injector;
    }
}
