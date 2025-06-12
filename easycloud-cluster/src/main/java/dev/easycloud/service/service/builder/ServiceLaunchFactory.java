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
            "-Xms128M", "-XX:-UseAdaptiveSizePolicy",
            "-XX:MaxRAMPercentage=95.0", "-Dterminal.jline=false"
    );

    @SneakyThrows
    public Process create(Service service) {
        var serviceFile = service.directory().resolve("easycloud-service.jar").toAbsolutePath();
        var mainClass = new JarInputStream(Files.newInputStream(serviceFile)).getManifest().getMainAttributes().getValue("Main-Class");

        List<String> dependencies = new ArrayList<>();
        var allowedDependencies = List.of("com.google", "com.fasterxml", "org.yaml", "io.activej", "org.jetbrains", "dev.easycloud.api");
        for (File file : Objects.requireNonNull(Path.of("resources").resolve("libraries").toFile().listFiles())) {
            if(allowedDependencies.stream().anyMatch(it -> file.getName().startsWith(it))) {
                dependencies.add(file.getAbsolutePath());
            }
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("java");
        arguments.add("-Xmx" + service.group().property(GroupProperties.MEMORY()) + "M");
        arguments.addAll(ARGUMENTS);
        arguments.add("-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener");
        arguments.add("-Dcom.mojang.eula.agree=true");
        arguments.add("-cp");

        var libraries = String.join(";", dependencies);
        if(!isWindows()) libraries = libraries.replace(";", ":");

        arguments.add(serviceFile + (isWindows() ? ";" : ":") + libraries);
        arguments.add("-javaagent:" + serviceFile);
        arguments.add(mainClass);
        if (service.group().platform().type().equals(PlatformType.SERVER)) {
            arguments.add("--max-players=" + service.group().property(GroupProperties.MAX_PLAYERS()));
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
