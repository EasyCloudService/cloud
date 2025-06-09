package dev.easycloud.service.scheduler;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class EasyScheduler {
    private final Runnable runnable;

    public void delay(long value) {
        new Thread(() -> {
            try {
                Thread.sleep(value);
                runnable.run();
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }).start();
    }

    public void repeat(long value) {
        new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(value);
                    runnable.run();
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }).start();
    }
}
