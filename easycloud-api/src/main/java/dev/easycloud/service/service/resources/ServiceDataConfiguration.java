package dev.easycloud.service.service.resources;

import dev.easycloud.service.file.resources.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@FileEntity(name = "service-data")
public final class ServiceDataConfiguration {
    private final String id;
    private final String key;

}