package dev.easycloud.service.terminal.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.awt.*;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum LogType {
    PRIMARY(new Color(56, 139, 219).getRGB()),
    SUCCESS(new Color(56, 219, 102).getRGB()),
    WARNING(new Color(248, 201, 13).getRGB()),
    ERROR(new Color(219, 56, 75).getRGB()),
    GRAY(new Color(166, 166, 166).getRGB()),
    WHITE(new Color(230, 230, 230).getRGB());

    private final int rgb;
}
