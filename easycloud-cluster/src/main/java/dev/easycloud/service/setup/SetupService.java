package dev.easycloud.service.setup;

import dev.easycloud.service.setup.resources.SetupData;
import dev.easycloud.service.setup.resources.SetupServiceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SetupService {
    SetupService add(SetupData<?> data);

    CompletableFuture<SetupServiceResult> publish();

    List<SetupService> running = new ArrayList<>();
    static SetupService simple() {
        return new SetupServiceImpl();
    }
}
