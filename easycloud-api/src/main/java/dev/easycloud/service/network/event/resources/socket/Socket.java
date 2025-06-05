package dev.easycloud.service.network.event.resources.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.net.socket.tcp.ITcpSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Socket {
    void run();

    <T extends Event> void read(Class<T> event, BiConsumer<ITcpSocket, T> onEvent);

    CompletableFuture<Void> waitForConnection = new CompletableFuture<>();
    default CompletableFuture<Void> waitForConnection() {;
        return waitForConnection;
    }

    void write(String message);
    void close();
}
