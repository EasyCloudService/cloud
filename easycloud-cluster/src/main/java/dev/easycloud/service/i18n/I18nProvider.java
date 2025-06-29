package dev.easycloud.service.i18n;

import dev.easycloud.service.EasyCloudCluster;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nProvider {
    private ResourceBundle bundle;

    public I18nProvider() {
        this.buildBundle();
    }

    public String get(String key, Object... args) {
        if(!this.bundle.containsKey(key)) {
            return String.format(Locale.ENGLISH, "Missing translation for key: %s", key);
        }
        var language = EasyCloudCluster.instance().configuration().local().language();
        if(!this.bundle.getLocale().equals(language)) {
            this.buildBundle();
        }
        return String.format(language, this.bundle.getString(key), args);
    }

    private void buildBundle() {
        this.bundle = ResourceBundle.getBundle("resources/i18n", EasyCloudCluster.instance().configuration().local().language());
    }
}
