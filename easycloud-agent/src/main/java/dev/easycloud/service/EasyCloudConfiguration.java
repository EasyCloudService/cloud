package dev.easycloud.service;

import dev.easycloud.service.file.resources.FileEntity;
import lombok.Getter;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@FileEntity(name = "config")
public class EasyCloudConfiguration {
    private final String key = "easyCloud" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000000, 99999999);
    private final Locale locale = Locale.ENGLISH;
}
