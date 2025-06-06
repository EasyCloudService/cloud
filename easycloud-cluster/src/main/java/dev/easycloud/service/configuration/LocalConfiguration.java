package dev.easycloud.service.configuration;

import dev.easycloud.service.file.resources.FileEntity;
import lombok.Getter;

import java.util.Locale;

@Getter
@FileEntity(name = "local")
public final class LocalConfiguration {
    private final Locale language = Locale.ENGLISH;
}
