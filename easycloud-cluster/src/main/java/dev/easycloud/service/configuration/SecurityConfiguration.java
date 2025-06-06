package dev.easycloud.service.configuration;

import dev.easycloud.service.file.resources.FileEntity;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@FileEntity(name = "security")
public final class SecurityConfiguration {
    private final String value = "easyCloud" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000000, 99999999);
}
