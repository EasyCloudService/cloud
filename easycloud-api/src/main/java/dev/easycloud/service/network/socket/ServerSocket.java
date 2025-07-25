package dev.easycloud.service.network.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.async.process.AsyncCloseable;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.consumer.ChannelConsumers;
import io.activej.csp.supplier.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.net.SimpleServer;
import io.activej.net.socket.tcp.ITcpSocket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@SuppressWarnings({"CallToPrintStackTrace", "unchecked"})
public final class ServerSocket implements Socket {
    private Eventloop eventloop;

    private final String securityKey;
    private final int port;

    private final List<ITcpSocket> sockets = new ArrayList<>();
    private final Map<Class<? extends Event>, List<BiConsumer<ITcpSocket, Event>>> eventHandlers = new HashMap<>();

    public ServerSocket(String securityKey, int port) {
        this.securityKey = securityKey;
        this.port = port;
    }

    @Override
    @SneakyThrows
    public void run() {
        this.eventloop = Eventloop.builder()
                .withFatalErrorHandler((throwable, o) -> throwable.printStackTrace())
                .withCurrentThread()
                .build();

        var server = SimpleServer.builder(this.eventloop, socket -> ChannelSuppliers.ofSocket(socket)
                .streamTo(ChannelConsumers.ofConsumer(byteBuf -> {
                    byte[] data = new byte[byteBuf.readRemaining()];
                    byteBuf.read(data);
                    var dataString = new String(data, UTF_8);

                    if(dataString.startsWith("SECURITY:") && !this.sockets.contains(socket)) {
                        if (!dataString.equals("SECURITY:" + this.securityKey)) {
                            log.error("Security key mismatch. Expected: {}, Received: {}", this.securityKey, dataString.replace("SECURITY:", ""));
                            socket.close();
                            return;
                        }
                        this.sockets.add(socket);
                        socket.write(ByteBuf.wrapForReading("SECURITY:ACCEPTED".getBytes()));
                        return;
                    }

                    if(!this.sockets.contains(socket)) {
                        log.error("Unauthorized socket connection attempt from");
                        return;
                    }

                    var eventClass = Class.forName(dataString.split("eventId")[1].split("\"")[2]);
                    var event = Event.deserialize(dataString, (Class<? extends Event>) eventClass);
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
                })))
                .withListenAddress(new InetSocketAddress(this.port))
                .build();

        server.listen();
        waitForConnection.complete(null);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                this.sockets.removeIf(ITcpSocket::isClosed);
            }
        }).start();

        this.eventloop.keepAlive(true);
        this.eventloop.run();
    }

    @Override
    public <T extends Event> void read(Class<T> event, BiConsumer<ITcpSocket, T> onEvent) {
        if (!this.eventHandlers.containsKey(event)) {
            this.eventHandlers.put(event, new ArrayList<>());
        }
        this.eventHandlers.get(event).add((BiConsumer<ITcpSocket, Event>) onEvent);
    }

    @Override
    public void write(byte[] bytes) {
        this.eventloop.execute(() -> {
            this.sockets.removeIf(ITcpSocket::isClosed);
            this.sockets.forEach(socket -> socket.write(ByteBuf.wrapForReading(bytes)));
        });
    }

    @Override
    public void close() {
        this.sockets.forEach(AsyncCloseable::close);
        this.sockets.clear();

        this.eventloop.breakEventloop();
        this.eventloop = null;
    }
}
