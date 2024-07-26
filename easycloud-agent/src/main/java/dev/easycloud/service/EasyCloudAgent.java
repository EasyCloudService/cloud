package dev.easycloud.service;

import dev.easycloud.service.terminal.SimpleTerminal;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class EasyCloudAgent extends CloudDriver {

    public EasyCloudAgent() {
        new SimpleTerminal();
    }

    @Override
    public EasyCloudAgent instance() {
        return this;
    }
}
