package dev.easycloud.service.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nProvider {
    private final ResourceBundle bundle = ResourceBundle.getBundle("resources/i18n", Locale.ENGLISH);

    public String get(String key) {
        return this.get(key, Locale.getDefault());
    }

    public String get(String key, Object... args) {
        return String.format(this.bundle.getString(key), args);
    }
}
