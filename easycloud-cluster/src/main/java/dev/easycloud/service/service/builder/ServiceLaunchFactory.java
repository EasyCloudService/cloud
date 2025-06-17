package dev.easycloud.service.service.builder;

import dev.easycloud.service.group.resources.GroupProperties;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.resources.ServiceProperties;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarInputStream;

@Slf4j
@UtilityClass
public final class ServiceLaunchFactory {
    private final List<String> ARGUMENTS = List.of(
            "-XX:+UseG1GC", "-Daikars.new.flags=true",
            "-XX:+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions", "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch", "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40", "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20", "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4", "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90", "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32", "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1", "-Dusing.aikars.flags=https://mcflags.emc.gs"
    );

    @SneakyThrows
    public Process create(Service service) {
        var serviceFile = service.directory().resolve("easycloud-service.jar").toAbsolutePath();
        var mainClass = new JarInputStream(Files.newInputStream(serviceFile)).getManifest().getMainAttributes().getValue("Main-Class");

        List<String> dependencies = new ArrayList<>();
        var allowedDependencies = List.of("com.google", "com.fasterxml", "org.yaml", "io.activej", "org.jetbrains", "dev.easycloud.api", "org.slf4j", "org.apache.logging");
        for (File file : Objects.requireNonNull(Path.of("resources").resolve("libs").toFile().listFiles())) {
            if(allowedDependencies.stream().anyMatch(it -> file.getName().startsWith(it))) {
                dependencies.add(file.getAbsolutePath());
            }
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("java");
        arguments.add("--enable-native-access=ALL-UNNAMED");
        arguments.add("--sun-misc-unsafe-memory-access=allow");
        arguments.add("-Xms" + service.group().read(GroupProperties.MEMORY()) + "M");
        arguments.add("-Xmx" + service.group().read(GroupProperties.MEMORY()) + "M");
        arguments.addAll(ARGUMENTS);

        arguments.add("-Dfile.encoding=UTF-8");
        arguments.add("-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener");
        arguments.add("-Dcom.mojang.eula.agree=true");
        arguments.add("-cp");

        var libraries = String.join(";", dependencies);
        if(!isWindows()) libraries = libraries.replace(";", ":");

        arguments.add(serviceFile + (isWindows() ? ";" : ":") + libraries);
        arguments.add("-javaagent:" + serviceFile);
        arguments.add(mainClass);
        if (service.group().getPlatform().type().equals(PlatformType.SERVER)) {
            arguments.add("--max-players=" + service.group().read(GroupProperties.MAX_PLAYERS()));
            arguments.add("--online-mode=false");
            arguments.add("nogui");
        }
        arguments.add("--port=" + service.property(ServiceProperties.PORT()));

        var builder = new ProcessBuilder(arguments);
        builder.directory(service.directory().toFile());
        return builder.start();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
