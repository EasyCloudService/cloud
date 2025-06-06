package dev.easycloud.service.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nProvider {
    private final ResourceBundle bundle;

    public I18nProvider(Locale locale) {
        this.bundle = ResourceBundle.getBundle("resources/i18n", locale);
    }

    public String get(String key, Object... args) {
        if(!this.bundle.containsKey(key)) {
            return String.format(Locale.ENGLISH, "Missing translation for key: %s", key);
        }

        return String.format(this.bundle.getLocale(), this.bundle.getString(key), args);
    }
}
