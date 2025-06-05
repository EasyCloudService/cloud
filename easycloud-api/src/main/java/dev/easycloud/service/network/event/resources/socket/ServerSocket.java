package dev.easycloud.service.network.event.resources.socket;

import dev.easycloud.service.network.event.Event;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import io.activej.bytebuf.ByteBuf;
import io.activej.eventloop.Eventloop;
import io.activej.net.SimpleServer;
import io.activej.net.socket.tcp.ITcpSocket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
public final class ServerSocket implements Socket {
    private SimpleServer server;

    private final Set<ITcpSocket> clients = ConcurrentHashMap.newKeySet();
    private final Map<Class<? extends Event>, List<BiConsumer<ITcpSocket, Event>>> eventHandlers = new HashMap<>();

    @Override
    @SneakyThrows
    public void run() {
        var eventLoop = Eventloop.builder().withCurrentThread().build();
        this.server = SimpleServer.builder(eventLoop, socket -> {
                    clients.add(socket);
                    log.info("Client connected");
                    this.read(socket);
                })
                .withListenPort(5200)
                .build();

        this.server.listen();
        eventLoop.run();

        waitForConnection.complete(null);
    }

    private void read(ITcpSocket socket) {
        socket.read().map(byteBuf -> {
            byte[] byteData = new byte[byteBuf.readRemaining()];
            byteBuf.read(byteData);
            var data = new String(byteData);

            if (data.equalsIgnoreCase("Ping")) {
                socket.write(ByteBuf.wrapForReading("Pong".getBytes()));
                return socket;
            }


            log.info(data.split("eventId")[1].split("\"")[2]);
            var eventClass = Class.forName(data.split("eventId")[1].split("\"")[2]);
            log.info("Found: " + eventClass.getSimpleName());


            var event = Event.deserialize(data, (Class<? extends Event>) eventClass);
            if (this.eventHandlers.containsKey(event.getClass())) {
                this.eventHandlers.get(event.getClass()).forEach(it -> {
                    try {
                        it.accept(socket, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Error processing event: {}", e.getMessage(), e);
                    }
                });
            }
            log.info(event.getClass().getSimpleName());
            log.info("Received: {}", data);

            return socket;
        }).whenComplete(() -> {
            this.read(socket);
        }).whenException(() -> {
            socket.close();
            clients.remove(socket);
            log.info("Client disconnected");
        });
    }

    @Override
    public <T extends Event> void read(Class<T> event, BiConsumer<ITcpSocket, T> onEvent) {
        log.info("Reading event: {}", event);
        if (!this.eventHandlers.containsKey(event)) {
            this.eventHandlers.put(event, new ArrayList<>());
        }
        this.eventHandlers.get(event).add((BiConsumer<ITcpSocket, Event>) onEvent);
    }

    @Override
    public void write(String message) {
        if (this.server == null) {
            log.warn("Server is not initialized. Cannot send message: {}", message);
            return;
        }

        var buffer = ByteBuf.wrapForReading(message.getBytes());
        for (ITcpSocket client : clients) {
            client.write(buffer);
        }
        buffer.recycle();
    }

    @Override
    public void close() {
        if (this.server == null) {
            log.warn("Server is not initialized. Cannot close.");
            return;
        }
        this.server.close();
    }
}
