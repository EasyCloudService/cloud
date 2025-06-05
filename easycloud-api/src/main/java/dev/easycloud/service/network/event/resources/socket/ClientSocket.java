package dev.easycloud.service.network.event.resources.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufs;
import io.activej.eventloop.Eventloop;
import io.activej.net.SimpleServer;
import io.activej.net.socket.tcp.ITcpSocket;
import io.activej.net.socket.tcp.TcpSocket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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

    public ClientSocket() {
    }

    public ClientSocket(ITcpSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        eventloop = Eventloop.builder().withCurrentThread().build();
        TcpSocket.connect(eventloop, new InetSocketAddress("127.0.0.1", 5200)).whenResult(socket -> {
            log.info("Connected to server");

            this.socket = socket;
            this.read(socket);
            this.keepAlive();
            waitForConnection.complete(null);
        }).whenException(exception -> {
            System.err.println("Failed to connect to server: " + exception.getMessage());
        });
        eventloop.run();
    }

    private void read(ITcpSocket socket) {
        socket.read()
                .map(byteBuf -> {
                    byte[] rawData = new byte[byteBuf.readRemaining()];
                    byteBuf.read(rawData);
                    var data = new String(rawData);

                    log.info("Received data: {}", data);

                    if (data.equalsIgnoreCase("Pong")) {
                        return this.socket;
                    }

                    log.info(data.split("eventId")[1].split("\"")[2]);
                    var eventClass = Class.forName(data.split("eventId")[1].split("\"")[2]);
                    log.info("Found: " + eventClass.getSimpleName());


                    var event = Event.deserialize(data, (Class<? extends Event>) eventClass);
                    if (this.eventHandlers.containsKey(event.getClass())) {
                        this.eventHandlers.get(event.getClass()).forEach(it -> {
                            try {
                                it.accept(null, event);
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Error processing event: {}", e.getMessage(), e);
                            }
                        });
                    }
                    return this.socket;
                })
                .whenComplete(() -> {
                    this.read(socket);
                })
                .whenException(exception -> {
                    log.error("Error reading from socket: {}", exception.getMessage(), exception);
                    exception.printStackTrace();
                    this.close();
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
        if (this.socket == null) {
            log.info("Socket is not connected. Cannot send message: {}", message);
            return;
        }

        this.eventloop.execute(() -> {
            var byteBuffer = ByteBuf.wrapForReading(message.getBytes());
            this.socket.write(byteBuffer);
        });
    }

    @Override
    public void close() {
        if (this.socket == null) {
            log.info("Socket is not connected. Cannot close.");
            return;
        }
        this.socket.close();
        this.socket = null;
    }
}
