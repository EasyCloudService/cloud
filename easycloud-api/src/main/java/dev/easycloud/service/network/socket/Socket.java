package dev.easycloud.service.network.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.net.socket.tcp.ITcpSocket;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Socket {
    void run();

    <T extends Event> void read(Class<T> event, BiConsumer<ITcpSocket, T> onEvent);

    CompletableFuture<Void> waitForConnection = new CompletableFuture<>();
    @SuppressWarnings("SameReturnValue")
    default CompletableFuture<Void> waitForConnection() {
        return waitForConnection;
    }

    void write(byte[] bytes);
    void close();
}
