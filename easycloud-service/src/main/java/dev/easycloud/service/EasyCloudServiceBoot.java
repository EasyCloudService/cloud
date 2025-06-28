package dev.easycloud.service;

import dev.easycloud.service.classloader.PlatformClassLoader;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public final class EasyCloudServiceBoot {
    @Getter
    private static Instrumentation instrumentation;
    private static Thread classLoaderThread;

    public static boolean loaded = false;

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting class with args: " + String.join(" ", args));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> classLoaderThread.interrupt()));

        var configuration = Configurations.Companion.read(Path.of(""), ServiceDataConfiguration.class);
        new Thread(() -> {
            var service = new EasyCloudService(configuration.key(), configuration.id(), configuration.clusterPort());
            service.load();
            service.run();
        }).start();

        while (loaded) {
            Thread.sleep(500);
        }

        classLoaderThread = PlatformClassLoader.inject(instrumentation, args);
    }
}
