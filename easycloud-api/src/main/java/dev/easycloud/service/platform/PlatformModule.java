package dev.easycloud.service.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PlatformModule {
    String platformId();

    String name();
    String version() default "1.0";
    String description() default "An easycloud platform module.";
    String[] authors() default {};
}
