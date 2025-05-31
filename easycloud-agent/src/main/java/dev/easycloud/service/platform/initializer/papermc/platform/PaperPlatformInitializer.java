package dev.easycloud.service.platform.initializer.papermc.platform;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.platform.initializer.papermc.AbstractPaperMCInitializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class PaperPlatformInitializer extends AbstractPaperMCInitializer {
    private final PlatformType type = PlatformType.SERVER;

    public PaperPlatformInitializer(String id) {
        super("paper");
    }

    @Override
    public void initialize(Path path) {
        var yaml = FileFactory.YAML;
        var configPath = path.resolve("config");
        configPath.toFile().mkdirs();

        Map<String, Object> proxies = new HashMap<>();

        Map<String, Object> bungeeCord = new HashMap<>();
        bungeeCord.put("online-mode", false);
        proxies.put("bungee-cord", bungeeCord);

        proxies.put("proxy-protocol", false);

        Map<String, Object> velocity = new HashMap<>();
        velocity.put("enabled", true);
        velocity.put("online-mode", true);
        velocity.put("secret", EasyCloudAgent.instance().configuration().key());
        proxies.put("velocity", velocity);

        Map<String, Object> yamlData;

        var file = configPath.resolve("paper-global.yml").toFile();
        if (file.exists()) {
            try (var reader = new FileReader(file)) {
                yamlData = yaml.load(reader);
                if (yamlData == null) {
                    yamlData = new HashMap<>();
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        } else {
            yamlData = new HashMap<>();
        }
        yamlData.put("proxies", proxies);

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(yamlData, writer);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<Platform> platforms() {
        List<Platform> tmp = new ArrayList<>();
        var versions = this.versions();
        if(versions == null) {
            log.error(EasyCloudAgent.instance().i18nProvider().get("group.platform.fetch.failed", "Paper"));
            return new ArrayList<>();
        }

        versions.forEach(version -> {
            tmp.add(new Platform(this.id, version, this.type));
        });
        return tmp;
    }
}
