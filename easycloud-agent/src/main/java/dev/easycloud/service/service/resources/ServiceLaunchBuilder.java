package dev.easycloud.service.service.resources;

import dev.easycloud.service.platform.PlatformType;
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
        arguments.add("-Xmx" + service.group().data().memory() + "M");
        arguments.addAll(ARGUMENTS);
        arguments.add("-Dcom.mojang.eula.agree=true");
        arguments.add("-jar");
        arguments.add("platform.jar");
        if(service.group().platform().type().equals(PlatformType.SERVER)) {
            arguments.add("--max-players=" + service.group().data().maxPlayers());
            //arguments.add("--nogui");
        }
        arguments.add("--port=" + service.port());

        var builder = new ProcessBuilder(arguments);
        builder.directory(service.directory().toFile());
        return builder.start();
    }
}
