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
                    Thread.sleep(value);
                    runnable.run();
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }).start();
    }

    public void repeat(long value, int times) {
        new Thread(() -> {
            for (int i = 0; i < times; i++) {
                try {
                    Thread.sleep(value);
                    runnable.run();
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }).start();
    }

}
