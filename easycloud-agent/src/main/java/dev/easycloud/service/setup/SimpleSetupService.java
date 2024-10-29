package dev.easycloud.service.setup;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.setup.resources.SetupServiceResult;
import dev.easycloud.service.terminal.completer.TerminalCompleter;
import dev.easycloud.service.terminal.logger.LoggerColor;
import dev.easycloud.service.terminal.logger.SimpleLogger;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.fusesource.jansi.Ansi.ansi;

@Getter
public final class SimpleSetupService implements SetupService {
    private final List<SetupData<?>> tmpSetupList = new ArrayList<>();

    @Override
    public SetupService add(SetupData<?> data) {
        this.tmpSetupList.add(data);
        return this;
    }

    private final Map<SetupData<?>, Object> answers = new HashMap<>();

    @Override
    public CompletableFuture<SetupServiceResult> publish() {
        EasyCloudAgent.instance().terminal().clear();

        SimpleLogger.info("");

        var future = new CompletableFuture<SetupServiceResult>();
        this.trigger(future);

        return future;
    }

    private Boolean error = false;
    private void trigger(CompletableFuture<SetupServiceResult> future) {
        new Thread(() -> {
            if(tmpSetupList.isEmpty()) {
                future.complete(new SetupServiceResult(this.answers));

                try {
                    Thread.sleep(250);
                    EasyCloudAgent.instance().terminal().clear();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            var current = this.tmpSetupList.getFirst();
            if(!this.error) {
                SimpleLogger.info(ansi().bgRgb(LoggerColor.PRIMARY.rgb()).a((this.answers.size() + 1) + ". ").a(current.question()).reset());

                if (current.possible() != null) {
                    SimpleLogger.info(ansi().a("* Possible answers: " + Arrays.toString(current.possible().toArray())));
                    current.possible().forEach(it -> TerminalCompleter.TEMP_VALUES().add(String.valueOf(it)));
                }
            }

            EasyCloudAgent.instance().terminal().readingThread().prioSub(line -> {
                if(current.possible() != null && current.possible().stream().noneMatch(it -> String.valueOf(it).equalsIgnoreCase(line.replace(" ", "")))) {
                    this.error = true;
                    trigger(future);
                    return;
                }
                this.error = false;
                TerminalCompleter.TEMP_VALUES().clear();

                SimpleLogger.info(ansi().fgRgb(LoggerColor.GRAY.rgb()).a("> ").a(line).reset());

                this.tmpSetupList.remove(current);
                this.answers.put(current, line.replace(" ", ""));

                trigger(future);
            });
        }).start();
    }

}