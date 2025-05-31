package dev.easycloud.service.service.resources;

import dev.easycloud.service.group.resources.Group;

import java.nio.file.Path;
import java.util.Map;

public interface Service {
    String id();
    Group group();

    /*<T> Map<T, Object> properties();
    default <T> T property(String key, Class<T> clazz) {
        return (T) properties().get(key);
    }*/

    ServiceState state();

    int port();
    String directoryRaw();

    default Path directory() {
        return Path.of(directoryRaw());
    }
}
