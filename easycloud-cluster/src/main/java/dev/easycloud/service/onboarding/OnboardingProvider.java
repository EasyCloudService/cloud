package dev.easycloud.service.onboarding;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class OnboardingProvider {
    public void run() {
        var i18n = EasyCloudClusterOld.instance().i18nProvider();
        var finished = new AtomicBoolean(false);
        EasyCloudClusterOld.instance().terminal().clear(false);

        this.write("");
        this.write(i18n.get("onboarding.welcome.1", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("EasyCloud").reset()));
        this.write(i18n.get("onboarding.welcome.2"));
        this.write("");
        this.write(i18n.get("onboarding.welcome.3"));
        this.write(i18n.get("onboarding.welcome.4", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("https://discord.gg/D5EKk9Cr2P").reset()));
        this.write("");
        this.write(i18n.get("onboarding.welcome.5"));
        this.write(ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("https://github.com/EasyCloudService/cloud/GUIDELINES.md").reset().toString());
        this.write("");
        this.write(i18n.get("onboarding.welcome.6", ansi().fgRgb(Log4jColor.PRIMARY.rgb()).a("EasyCloud").reset()));
        this.write(i18n.get("onboarding.welcome.7"));
        this.write("");
        EasyCloudClusterOld.instance().terminal().update();

        var completer = (TerminalCompleter) EasyCloudClusterOld.instance().terminal().lineReader().getCompleter();
        completer.possibleResults().add(new Candidate("yes"));
        completer.possibleResults().add(new Candidate("no"));

        EasyCloudClusterOld.instance().terminal().readingThread().priority(line -> {
            completer.possibleResults().clear();
            if (line.toLowerCase().startsWith("yes")) {
                EasyCloudClusterOld.instance().terminal().clear();
                finished.set(true);
                return;
            }
            EasyCloudClusterOld.instance().terminal().clear();
            System.exit(0);
        });


        while (!finished.get()) {
        }
    }

    private void write(String text) {
        var writer = EasyCloudClusterOld.instance().terminal().terminal().writer();
        writer.println("    " + text);
    }

}
