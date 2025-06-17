package dev.easycloud.service;

import dev.easycloud.service.classloader.PlatformClassLoader;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import lombok.Getter;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public final class EasyCloudServiceBoot {
    @Getter
    private static Instrumentation instrumentation;
    private static Thread classLoaderThread;

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void main(String[] args) {
        System.out.println("Starting class with args: " + String.join(" ", args));
        classLoaderThread = PlatformClassLoader.inject(instrumentation, args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> classLoaderThread.interrupt()));

        var configuration = Configurations.Companion.read(Path.of(""), ServiceDataConfiguration.class);
        new EasyCloudService(configuration.key(), configuration.clusterPort(), configuration.id());
    }
}
