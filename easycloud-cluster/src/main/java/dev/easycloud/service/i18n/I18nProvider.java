package dev.easycloud.service.i18n;

import com.google.inject.Inject;
import dev.easycloud.service.configuration.ClusterConfiguration;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nProvider {
    private final ClusterConfiguration configurations;
    private ResourceBundle bundle;

    @Inject
    public I18nProvider(ClusterConfiguration configurations) {
        this.configurations = configurations;

        this.buildBundle();
    }

    public String get(String key, Object... args) {
        if(!this.bundle.containsKey(key)) {
            return String.format(Locale.ENGLISH, "Missing translation for key: %s", key);
        }
        var language = this.configurations.local.getLanguage();
        if(!this.bundle.getLocale().equals(language)) {
            this.buildBundle();
        }
        return String.format(language, this.bundle.getString(key), args);
    }

    private void buildBundle() {
        this.bundle = ResourceBundle.getBundle("i18n", this.configurations.local.getLanguage());
    }
}
