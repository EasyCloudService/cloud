package dev.easycloud.service.service.resources;

import dev.easycloud.service.EasyCloudAgent;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ServiceFileFactory {

    @SneakyThrows
    public void insert(Service service, Path path) {
        if(service.group().platform().initilizerId().equals("paper")) {
            var configPath = path.resolve("config");
            configPath.toFile().mkdirs();
            paper(configPath);
            return;
        }

        if(service.group().platform().initilizerId().equals("velocity")) {
            if(!Files.exists(path.resolve("velocity.toml"))) {
                Files.copy(EasyCloudAgent.class.getClassLoader().getResourceAsStream("velocity.toml"), path.resolve("velocity.toml"));
            }
            return;
        }
    }

    private final Yaml yaml = new Yaml();
    private static void paper(Path path) {
        Map<String, Object> proxies = new HashMap<>();

        Map<String, Object> bungeeCord = new HashMap<>();
        bungeeCord.put("online-mode", false);
        proxies.put("bungee-cord", bungeeCord);

        proxies.put("proxy-protocol", false);

        Map<String, Object> velocity = new HashMap<>();
        velocity.put("enabled", true);
        velocity.put("online-mode", true);
        velocity.put("secret", EasyCloudAgent.instance().securityKey());
        proxies.put("velocity", velocity);

        Map<String, Object> yamlData = new HashMap<>();
        yamlData.put("proxies", proxies);

        var file = path.resolve("paper-global.yml").toFile();
        if(file.exists()) {
            file.delete();
        }

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(yamlData, writer);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
