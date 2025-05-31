package dev.easycloud.service.update.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ReleaseInformation {
    private final String latestVersion;
    private final String downloadUrl;

}
