package dev.easycloud.service.platform.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.request.RequestFactory;
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
public class PaperPlatformInitializer implements PlatformInitializer {
    private final String id = "paper";
    private final String url = "https://api.papermc.io/v2/projects/paper";
    private final PlatformType type = PlatformType.SERVER;

    private List<String> versions() {
        var response = RequestFactory.getRequest(this.url);
        if(response == null) {
            return null;
        }

        var json = FileFactory.GSON.fromJson(response, JsonObject.class);
        var versions = json.getAsJsonArray("versions");

        List<String> versionList = new ArrayList<>();
        for (JsonElement version : versions) {
            versionList.add(version.getAsString());
        }
        return versionList;
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
    public String buildDownload(String version) {
        var apiUrl = this.url + "/versions/" + version + "/builds";
        var response = RequestFactory.getRequest(apiUrl);
        var json = FileFactory.GSON.fromJson(response, JsonObject.class);
        if(json == null) {
            return null;
        }

        var builds = json.getAsJsonArray("builds");

        Integer latestBuild = null;
        for (JsonElement element : builds) {
            var build = element.getAsJsonObject();
            var channel = build.get("channel").getAsString();
            if ("default".equals(channel) || "experimental".equals(channel)) {
                log.info(build.toString());
                int buildNumber = build.get("build").getAsInt();
                if (latestBuild == null || buildNumber > latestBuild) {
                    latestBuild = buildNumber;
                }
            }
        }

        return this.url
                + "/versions/" + version
                + "/builds/" + latestBuild
                + "/downloads/paper-" + version + "-" + latestBuild + ".jar";
    }

    @Override
    public List<Platform> platforms() {
        List<Platform> tmp = new ArrayList<>();
        var versions = this.versions();
        if(versions == null) {
            log.error(EasyCloudAgent.instance().i18nProvider().get("group.platform.fetch.failed", "PaperMC"));
            return new ArrayList<>();
        }

        versions.forEach(version -> {
            tmp.add(new Platform(this.id, version, this.type));
        });
        return tmp;
    }
}
