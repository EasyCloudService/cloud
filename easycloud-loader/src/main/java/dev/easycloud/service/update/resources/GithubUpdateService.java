package dev.easycloud.service.update.resources;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public final class GithubUpdateService {
    private final String URL = "https://api.github.com/repos/EasyCloudService/cloud/releases/latest";
    @Getter
    private final ReleaseInformation information;

    @SneakyThrows
    public GithubUpdateService() {
        var url = new URL(this.URL);
        var conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        var responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to check for updates: " + responseCode);
        }

        var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        var response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }
        reader.close();

        var jsonResponse = response.toString();
        this.information = parseJson(jsonResponse);
    }

    private ReleaseInformation parseJson(String jsonResponse) {
        var tagPattern = Pattern.compile("\"name\":\"(.*?)\"");
        var tagMatcher = tagPattern.matcher(jsonResponse);
        String latestVersion = null;
        if (tagMatcher.find()) {
            latestVersion = tagMatcher.group(1);
        }

        var assetPattern = Pattern.compile("\"browser_download_url\":\"(.*?easycloud-loader.jar)\"");
        var assetMatcher = assetPattern.matcher(jsonResponse);
        String downloadUrl = null;
        if (assetMatcher.find()) {
            downloadUrl = assetMatcher.group(1);
        }
        return new ReleaseInformation(latestVersion, downloadUrl);
    }

    public void download() {
        var name = "tmp-loader.jar";
        try (var in = new BufferedInputStream(new URL(this.information.getDownloadUrl()).openStream());
             var fileOutputStream = new FileOutputStream(name)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("Download finished: " + name);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
