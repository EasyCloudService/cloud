package dev.easycloud.service.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nProvider {
    private final Locale locale;
    private final ResourceBundle bundle;

    public I18nProvider(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle("resources/i18n", this.locale);
    }

    public String get(String key, Object... args) {
        return String.format(this.bundle.getLocale(), this.bundle.getString(key), args);
    }
}
