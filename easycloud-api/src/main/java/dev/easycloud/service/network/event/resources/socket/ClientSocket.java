package dev.easycloud.service.network.event.resources.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.bytebuf.ByteBuf;
import io.activej.eventloop.Eventloop;
import io.activej.net.SimpleServer;
import io.activej.net.socket.tcp.ITcpSocket;
import io.activej.net.socket.tcp.TcpSocket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public final class ClientSocket implements Socket {
    private Eventloop eventloop;
    private ITcpSocket socket;

    private final Map<Class<? extends Event>, List<BiConsumer<ITcpSocket, Event>>> eventHandlers = new HashMap<>();

    @Override
    public void run() {
        eventloop = Eventloop.builder().withCurrentThread().build();
        TcpSocket.connect(eventloop, new InetSocketAddress("127.0.0.1", 5200)).whenResult(socket -> {
            log.info("Connected to server");

            this.socket = socket;
            this.reading();
            this.keepAlive();
            waitForConnection.complete(null);
        }).whenException(exception -> {
            System.err.println("Failed to connect to server: " + exception.getMessage());
        });
        eventloop.run();
    }

    private void reading() {
        this.socket.read()
                .map(byteBuf -> {
                    byte[] data = new byte[byteBuf.readRemaining()];
                    byteBuf.read(data);
                    String message = new String(data);
                    log.info("Received data: {}", message);
                    return this.socket;
                })
                .whenComplete(() -> {
                    log.info("Socket closed");
                });
    }

    private void keepAlive() {
        if (socket == null || socket.isClosed()) return;
        eventloop.delayBackground(Duration.ofSeconds(10), () -> {
            write("Ping");
            keepAlive();
        });
    }

    @Override
    public <T extends Event> void read(Class<T> event, BiConsumer<ITcpSocket, T> onEvent) {
        if (!this.eventHandlers.containsKey(event)) {
            this.eventHandlers.put(event, new ArrayList<>());
        }
        this.eventHandlers.get(event).add((BiConsumer<ITcpSocket, Event>) onEvent);
    }

    @Override
    public void write(String message) {
        if(this.socket == null) {
            log.info("Socket is not connected. Cannot send message: {}", message);
            return;
        }

        this.eventloop.execute(() -> {
            var buffer = ByteBuf.wrapForReading(message.getBytes());
            this.socket.write(buffer);
        });
    }

    @Override
    public void close() {
        if(this.socket == null) {
            log.info("Socket is not connected. Cannot close.");
            return;
        }
        this.socket.close();
    }
}
