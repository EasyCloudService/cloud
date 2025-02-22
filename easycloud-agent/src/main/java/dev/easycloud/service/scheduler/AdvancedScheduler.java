package dev.easycloud.service.scheduler;

import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public final class AdvancedScheduler {
    private final Function<Void, Boolean> runnable;
    
    public void run(long every) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(every);
                    if (!runnable.apply(null)) {
                        break;
                    }
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }).start();
    }
}
