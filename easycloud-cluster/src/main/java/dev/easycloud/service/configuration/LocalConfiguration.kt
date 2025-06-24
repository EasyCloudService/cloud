package dev.easycloud.service.configuration;

import dev.easycloud.service.configuration.resources.ConfigurationEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
@ConfigurationEntity(name = "local")
public final class LocalConfiguration {
    private Locale language = Locale.ENGLISH;
    private boolean announceUpdates = true;
    private int clusterPort = 5200;
    private int proxyPort = 25565;
    private int startingSameTime = 3;
    private int dynamicPercentage = 80;
}
