package dev.easycloud.service.network.event.resources.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.async.process.AsyncCloseable;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.bytebuf.ByteBufStrings;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.binary.decoder.ByteBufsDecoders;
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

import static io.activej.bytebuf.ByteBufStrings.CR;
import static io.activej.bytebuf.ByteBufStrings.LF;
import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
public final class ServerSocket implements Socket {
    private Eventloop eventloop;

    private final byte[] CRLF = {CR, LF};
    private final List<ITcpSocket> sockets = new ArrayList<>();
    private final Map<Class<? extends Event>, List<BiConsumer<ITcpSocket, Event>>> eventHandlers = new HashMap<>();

    @Override
    @SneakyThrows
    public void run() {
        this.eventloop = Eventloop.builder()
                .withFatalErrorHandler((throwable, o) -> {
                    throwable.printStackTrace();
                })
                .withCurrentThread()
                .build();

        var server = SimpleServer.builder(this.eventloop, socket -> {
                    ChannelSuppliers.ofSocket(socket)
                            .streamTo(ChannelConsumers.ofConsumer(byteBuf -> {
                                byte[] data = new byte[byteBuf.readRemaining()];
                                byteBuf.read(data);
                                var dataString = new String(data, UTF_8);

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
                            }));


                    this.sockets.add(socket);
                })
                .withListenAddress(new InetSocketAddress(5200))
                .build();

        server.listen();
        waitForConnection.complete(null);

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
        this.sockets.forEach(socket -> {
            socket.write(ByteBuf.wrapForReading(bytes));
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
