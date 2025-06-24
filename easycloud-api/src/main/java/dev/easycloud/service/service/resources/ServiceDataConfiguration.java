package dev.easycloud.service.service.resources;

import dev.easycloud.service.configuration.resources.ConfigurationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationEntity(name = "service-data")
@SuppressWarnings("ClassCanBeRecord")
public final class ServiceDataConfiguration {
    private final String id;
    private final String key;
    private final int clusterPort;

}