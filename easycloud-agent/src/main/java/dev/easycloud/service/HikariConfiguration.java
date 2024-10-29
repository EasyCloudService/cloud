package dev.easycloud.service;

import dev.easycloud.service.file.resources.FileEntity;
import dev.httpmarco.evelon.layer.connection.ConnectionAuthentication;
import lombok.Getter;

@Getter
@FileEntity(name = "evelon")
public class HikariConfiguration extends ConnectionAuthentication {
    private final String path;

    public HikariConfiguration() {
        super("H2", true);
        this.path = "storage/db.h2";
    }
}