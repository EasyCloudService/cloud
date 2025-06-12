package dev.easycloud.service.onboarding;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.setup.SetupService;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class OnboardingProvider {
    public void run() {
        var i18n = EasyCloudCluster.instance().i18nProvider();
        var finished = new AtomicBoolean(false);
        EasyCloudCluster.instance().terminal().clear(false);

        this.write("");
        this.write(i18n.get("onboarding.welcome.1", ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset()));
        this.write(i18n.get("onboarding.welcome.2"));
        this.write("");
        this.write(i18n.get("onboarding.welcome.3"));
        this.write(i18n.get("onboarding.welcome.4", ansi().fgRgb(LogType.PRIMARY.rgb()).a("https://discord.gg/D5EKk9Cr2P").reset()));
        this.write("");
        this.write(i18n.get("onboarding.welcome.5"));
        this.write(ansi().fgRgb(LogType.PRIMARY.rgb()).a("https://github.com/EasyCloudService/cloud/GUIDELINES.md").reset().toString());
        this.write("");
        this.write(i18n.get("onboarding.welcome.6", ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset()));
        this.write(i18n.get("onboarding.welcome.7"));
        this.write("");
        EasyCloudCluster.instance().terminal().update();

        var completer = (TerminalCompleter) EasyCloudCluster.instance().terminal().lineReader().getCompleter();
        completer.possibleResults().add(new Candidate("yes"));
        completer.possibleResults().add(new Candidate("no"));

        EasyCloudCluster.instance().terminal().readingThread().priority(line -> {
            completer.possibleResults().clear();
            if (line.toLowerCase().startsWith("yes")) {
                EasyCloudCluster.instance().terminal().clear();
                SetupService.simple()
                        .add(new SetupData<String>("language", i18n.get("onboarding.setup.language"), List.of("de", "en")))
                        .add(new SetupData<String>("update", i18n.get("onboarding.setup.update"), List.of("yes", "no")))
                        .add(new SetupData<Integer>("proxy_port", i18n.get("onboarding.setup.proxy.port"), null))
                        .publish()
                        .thenAccept(it -> {
                            if(it.answers().isEmpty()) {
                                EasyCloudCluster.instance().terminal().clear();
                                finished.set(true);
                                return;
                            }

                            var configuration = EasyCloudCluster.instance().configuration().local();
                            configuration.language(it.result("language", String.class).equalsIgnoreCase("de") ? Locale.GERMAN : Locale.ENGLISH);
                            configuration.announceUpdates(it.result("update", String.class).equalsIgnoreCase("yes"));
                            configuration.proxyPort(it.result("proxy_port", Integer.class));

                            EasyCloudCluster.instance().configuration().publish(configuration);

                            finished.set(true);

                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    log.info("Onboarding completed. Welcome to @{}!", ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset());
                                    log.info("Following command can be used to create a group: '{}'", ansi().fgRgb(LogType.PRIMARY.rgb()).a("group setup").reset());
                                } catch (InterruptedException exception) {
                                    throw new RuntimeException(exception);
                                }
                            }).start();
                        });
                return;
            }
            EasyCloudCluster.instance().terminal().clear();
            finished.set(true);
        });


        while (!finished.get()) {
        }
    }

    private void write(String text) {
        var writer = EasyCloudCluster.instance().terminal().terminal().writer();
        writer.println("    " + text);
    }

}
