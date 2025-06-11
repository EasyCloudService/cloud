package dev.easycloud.service.onboarding;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.fusesource.jansi.Ansi.ansi;

@Slf4j
public final class OnboardingProvider {
    public void run() {
        var finished = new AtomicBoolean(false);
        EasyCloudCluster.instance().terminal().clear(false);

        this.write("");
        this.write("Welcome to @" + ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset() + "!");
        this.write("This is the onboarding process, which will help you to set up your Cloud.");
        this.write("");
        this.write("Thanks to all contributors for their work on EasyCloudService.");
        this.write("If you have need help, please join our Discord: " + ansi().fgRgb(LogType.PRIMARY.rgb()).a("https://discord.gg/D5EKk9Cr2P").reset());
        this.write("");
        this.write("Please read our rules and guidelines before using EasyCloudService:");
        this.write(ansi().fgRgb(LogType.PRIMARY.rgb()).a("https://github.com/EasyCloudService/cloud/guidelines.md").reset().toString());
        this.write("");
        this.write("If you are using EasyCloud you agree to the guidelines and rules of @" + ansi().fgRgb(LogType.PRIMARY.rgb()).a("EasyCloud").reset() + ".");
        this.write("Do you want to continue? (yes/no)");
        this.write("");
        EasyCloudCluster.instance().terminal().update();

        var completer = (TerminalCompleter) EasyCloudCluster.instance().terminal().lineReader().getCompleter();
        completer.possibleResults().add(new Candidate("yes"));
        completer.possibleResults().add(new Candidate("no"));

        EasyCloudCluster.instance().terminal().readingThread().priority(line -> {
            completer.possibleResults().clear();
            if(line.equalsIgnoreCase("yes")) {
                finished.set(true);
                EasyCloudCluster.instance().terminal().clear();
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        log.info("Following command can be used to create a group: '{}'", ansi().fgRgb(LogType.PRIMARY.rgb()).a("group setup").reset());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                return;
            }
            log.info("User aborted the onboarding process.");
            System.exit(0);
        });

        while (!finished.get()) {
        }
    }

    private void write(String text) {
        var writer = EasyCloudCluster.instance().terminal().terminal().writer();
        writer.println("    " + text);
    }

}
