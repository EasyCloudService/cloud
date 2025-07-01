package dev.easycloud.service.request;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public final class RequestFactory {

    @SneakyThrows
    public String getRequest(String url) {
        try {
            @SuppressWarnings("deprecation")
            var connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != 200) {
                System.err.println("API request failed: HTTP " + connection.getResponseCode());
                return null;
            }

            var response = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } catch (IOException exception) {
            System.err.println("Failed to make API request: " + exception.getMessage());
        }
        return null;
    }
}
