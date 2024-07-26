package dev.easycloud.service;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class CloudDriver {


    public CloudDriver instance() {
        return this;
    }

}
