package dev.easycloud.service.setup;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.setup.resources.SetupServiceResult;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.Candidate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Slf4j
public final class SetupServiceImpl implements SetupService {
    private final List<SetupData<?>> tempSetupList = new ArrayList<>();
    private final Map<SetupData<?>, String> answers = new HashMap<>();

    @Override
    public SetupService add(SetupData<?> data) {
        this.tempSetupList.add(data);
        return this;
    }

    @Override
    public CompletableFuture<SetupServiceResult> publish() {
        SetupService.running.add(this);

        EasyCloudClusterOld.instance().terminal().clear();

        this.print(ansi().a("Write ").fgRgb(Log4jColor.ERROR.rgb()).a("cancel").reset().a(" to abort the setup.").toString());

        var future = new CompletableFuture<SetupServiceResult>();
        this.trigger(future);

        return future;
    }

    private Boolean error = false;
    private void trigger(CompletableFuture<SetupServiceResult> future) {
        new Thread(() -> {
            var completer = (TerminalCompleter) EasyCloudClusterOld.instance().terminal().lineReader().getCompleter();

            if (tempSetupList.isEmpty()) {
                SetupService.running.remove(this);
                EasyCloudClusterOld.instance().terminal().revert();
                future.complete(new SetupServiceResult(this.answers));
                completer.possibleResults().clear();
                return;
            }
            var current = this.tempSetupList.getFirst();
            if (!this.error) {
                this.print(ansi().bgRgb(Log4jColor.PRIMARY.rgb()).fgRgb(Log4jColor.WHITE.rgb()).a((this.answers.size() + 1) + ". ").a(current.question()).reset().toString());

                if (current.possible() != null) {
                    this.print(ansi().a("* For possible answers use 'tab'").toString());
                    current.possible().forEach(it -> {
                        completer.possibleResults().add(new Candidate(String.valueOf(it), String.valueOf(it), null, null, null, null, true));
                    });
                } else {
                    completer.possibleResults().add(new Candidate(""));
                }
            }

            EasyCloudClusterOld.instance().terminal().readingThread().priority(line -> {
                if(line.equalsIgnoreCase("cancel")) {
                    SetupService.running.remove(this);
                    completer.possibleResults().clear();
                    EasyCloudClusterOld.instance().terminal().revert();
                    this.print(ansi().fgRgb(Log4jColor.ERROR.rgb()).a(EasyCloudClusterOld.instance().i18nProvider().get("global.setup.cancel")).toString());
                    future.complete(new SetupServiceResult(new HashMap<>()));
                    return;
                }

                if (current.possible() != null && current.possible().stream().noneMatch(it -> String.valueOf(it).equalsIgnoreCase(line.replace(" ", "")))) {
                    this.error = true;
                    this.trigger(future);
                    return;
                }
                this.error = false;
                completer.possibleResults().clear();

                this.print(ansi().fgRgb(Log4jColor.GRAY.rgb()).a("> ").a(line).reset().toString());

                this.tempSetupList.remove(current);
                this.answers.put(current, line.replace(" ", ""));

                this.trigger(future);
            });
        }).start();
    }

    private void print(String text) {
        log.info("SETUP: {}", text);
    }

}
