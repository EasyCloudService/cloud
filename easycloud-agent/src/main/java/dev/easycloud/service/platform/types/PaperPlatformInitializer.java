package dev.easycloud.service.platform.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.platform.Platform;
import dev.easycloud.service.platform.PlatformType;
import dev.easycloud.service.request.RequestFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
    public String buildDownload(String version) {
        var apiUrl = this.url + "/versions/" + version + "/builds";
        var response = RequestFactory.getRequest(apiUrl);
        var json = FileFactory.GSON.fromJson(response, JsonObject.class);
        var builds = json.getAsJsonArray("builds");

        Integer latestBuild = null;
        for (JsonElement element : builds) {
            var build = element.getAsJsonObject();
            if ("default".equals(build.get("channel").getAsString())) {
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
            log.error("Failed to fetch PaperMC versions.");
            return new ArrayList<>();
        }

        versions.forEach(version -> {
            tmp.add(new Platform("paper-" + version, this.id, version, this.type));
        });
        return tmp;
    }
}
