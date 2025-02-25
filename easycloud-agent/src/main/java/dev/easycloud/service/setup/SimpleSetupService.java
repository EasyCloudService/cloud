package dev.easycloud.service.setup;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.setup.resources.SetupServiceResult;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.LogType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
@Slf4j
public final class SimpleSetupService implements SetupService {
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

        EasyCloudAgent.instance().terminal().clear();

        log.info(ansi().a("Write ").fgRgb(LogType.ERROR.rgb()).a("cancel").reset().a(" to cancel the setup.").toString());

        var future = new CompletableFuture<SetupServiceResult>();
        this.trigger(future);

        return future;
    }

    private Boolean error = false;
    private void trigger(CompletableFuture<SetupServiceResult> future) {
        new Thread(() -> {
            if (tempSetupList.isEmpty()) {
                SetupService.running.remove(this);
                EasyCloudAgent.instance().terminal().revert();
                future.complete(new SetupServiceResult(this.answers));
                return;
            }
            var current = this.tempSetupList.getFirst();
            if (!this.error) {
                this.print(ansi().bgRgb(LogType.PRIMARY.rgb()).a((this.answers.size() + 1) + ". ").a(current.question()).reset().toString());

                if (current.possible() != null) {
                    this.print(ansi().a("* For possible answers use tab complete").toString());
                    current.possible().forEach(it -> TerminalCompleter.TEMP_VALUES().add(String.valueOf(it)));
                }
            }

            EasyCloudAgent.instance().terminal().readingThread().prioSub(line -> {
                if(line.equalsIgnoreCase("cancel")) {
                    SetupService.running.remove(this);
                    TerminalCompleter.TEMP_VALUES().clear();
                    EasyCloudAgent.instance().terminal().revert();
                    this.print(ansi().fgRgb(LogType.ERROR.rgb()).a("Setup was canceled.").toString());
                    future.complete(new SetupServiceResult(new HashMap<>()));
                    return;
                }

                if (current.possible() != null && current.possible().stream().noneMatch(it -> String.valueOf(it).equalsIgnoreCase(line.replace(" ", "")))) {
                    this.error = true;
                    this.trigger(future);
                    return;
                }
                this.error = false;
                TerminalCompleter.TEMP_VALUES().clear();

                this.print(ansi().fgRgb(LogType.GRAY.rgb()).a("> ").a(line).reset().toString());

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
