package dev.easycloud.service.file.ressources;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FileEntity {
    String name();
}
