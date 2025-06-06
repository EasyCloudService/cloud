package dev.easycloud.service.service.builder;

import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.resources.property.DefaultProperty;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public final class ServiceLaunchBuilder {
    private final List<String> ARGUMENTS = List.of(
            "-Xms128M", "-XX:-UseAdaptiveSizePolicy",
            "-XX:MaxRAMPercentage=95.0", "-Dterminal.jline=false"
    );

    @SneakyThrows
    public Process create(Service service) {
        List<String> arguments = new ArrayList<>();
        arguments.add("java");
        arguments.add("-Xmx" + service.group().properties().memory() + "M");
        arguments.addAll(ARGUMENTS);
        arguments.add("-Dcom.mojang.eula.agree=true");
        arguments.add("-jar");
        arguments.add("platform.jar");
        if (service.group().platform().type().equals(PlatformType.SERVER)) {
            arguments.add("--max-players=" + service.group().properties().maxPlayers());
            arguments.add("--online-mode=false");
            arguments.add("nogui");
        }
        arguments.add("--port=" + service.property(DefaultProperty.PORT()));

        var builder = new ProcessBuilder(arguments);
        builder.directory(service.directory().toFile());
        return builder.start();
    }
}
