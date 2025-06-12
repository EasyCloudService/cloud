package dev.easycloud.service.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ModuleConfiguration {
    private final String[] platforms;
    private final String name;
}
