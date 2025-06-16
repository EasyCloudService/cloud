package dev.easycloud.service.configuration;

import dev.easycloud.service.configuration.resources.ConfigurationEntity;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@ConfigurationEntity(name = "security")
public final class SecurityConfiguration {
    private final String value = "easyCloud" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000000, 99999999);
}
