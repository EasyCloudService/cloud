package dev.easycloud.service;

import dev.easycloud.service.classloader.PlatformClassLoader;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.service.resources.ServiceDataConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public final class EasyCloudServiceBoot {
    @Getter
    private static Instrumentation instrumentation;
    private static Thread classLoaderThread;

    public static boolean loaded = false;

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting class with args: {}", String.join(" ", args));
        System.setProperty("log4j.configurationFile",Objects.requireNonNull(EasyCloudServiceBoot.class.getClassLoader().getResource("log4j2.xml")).toString());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> classLoaderThread.interrupt()));

        var configuration = Configurations.Companion.read(Path.of(""), ServiceDataConfiguration.class);
        new Thread(() -> {
            var service = new EasyCloudService(configuration.key(), configuration.id(), configuration.clusterPort());
            service.load();
            service.run();
        }).start();

        while (!loaded) {
            Thread.sleep(250);
        }

        classLoaderThread = PlatformClassLoader.inject(instrumentation, args);
    }
}
