package dev.easycloud.service.update;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubReleaseInformation {
    private final String latestVersion;
    private final String downloadUrl;

}
