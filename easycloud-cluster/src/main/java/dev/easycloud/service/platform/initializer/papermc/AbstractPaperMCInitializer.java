package dev.easycloud.service.platform.initializer.papermc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.easycloud.service.configuration.Configurations;
import dev.easycloud.service.platform.PlatformInitializer;
import dev.easycloud.service.request.RequestFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
@AllArgsConstructor
public abstract class AbstractPaperMCInitializer implements PlatformInitializer {
    protected final String id;
    protected final String url;

    protected List<String> versions() {
        var response = RequestFactory.getRequest(this.url);
        if(response == null) {
            return null;
        }

        var json = Configurations.Companion.getGson().fromJson(response, JsonObject.class);
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
        var json = Configurations.Companion.getGson().fromJson(response, JsonObject.class);
        if(json == null) {
            return null;
        }

        var builds = json.getAsJsonArray("builds");
        var latestBuild = latestBuild(builds);

        return this.url
                + "/versions/" + version
                + "/builds/" + latestBuild
                + "/downloads/" + this.id + "-" + version + "-" + latestBuild + ".jar";
    }

    private static @Nullable Integer latestBuild(JsonArray builds) {
        Integer latestBuild = null;
        for (JsonElement element : builds) {
            var build = element.getAsJsonObject();
            var channel = build.get("channel").getAsString();
            if ("default".equals(channel) || "experimental".equals(channel)) {
                //log.info(build.toString());
                int buildNumber = build.get("build").getAsInt();
                if (latestBuild == null || buildNumber > latestBuild) {
                    latestBuild = buildNumber;
                }
            }
        }
        return latestBuild;
    }
}
