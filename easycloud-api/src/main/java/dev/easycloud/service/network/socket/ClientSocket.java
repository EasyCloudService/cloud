package dev.easycloud.service.network.socket;

import dev.easycloud.service.network.event.Event;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.consumer.ChannelConsumers;
import io.activej.csp.supplier.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.ITcpSocket;
import io.activej.net.socket.tcp.TcpSocket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class ClientSocket implements Socket {
    private Eventloop eventloop;
    private ITcpSocket socket;

    private final String securityKey;
    private final Map<Class<? extends Event>, List<BiConsumer<ITcpSocket, Event>>> eventHandlers = new HashMap<>();

    public ClientSocket(String securityKey) {
        this.securityKey = securityKey;
    }

    @Override
    public void run() {
        this.eventloop = Eventloop.builder()
                .withFatalErrorHandler((throwable, o) -> {
                    throwable.printStackTrace();
                })
                .withCurrentThread()
                .build();
        this.eventloop.connect(new InetSocketAddress("127.0.0.1", 5200), (channel, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
                return;
            }

            try {
                this.socket = TcpSocket.wrapChannel(eventloop, channel, null);
                log.info("Connected to server: {}", channel.getRemoteAddress());
                this.write(("SECURITY:" + this.securityKey).getBytes());
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }

            ChannelSuppliers.ofSocket(socket)
                    .streamTo(ChannelConsumers.ofConsumer(byteBuf -> {
                        byte[] data = new byte[byteBuf.readRemaining()];
                        byteBuf.read(data);
                        var dataString = new String(data, UTF_8);

                        if(dataString.equals("SECURITY:ACCEPTED")) {
                            log.info("Security key accepted.");
                            waitForConnection.complete(null);
                            return;
                        }

                        var eventClass = Class.forName(dataString.split("eventId")[1].split("\"")[2]);
                        var event = Event.deserialize(dataString, (Class<? extends Event>) eventClass);
                        if (this.eventHandlers.containsKey(event.getClass())) {
                            this.eventHandlers.get(event.getClass()).forEach(it -> {
                                try {
                                    it.accept(null, event);
                                } catch (Exception exception3) {
                                    exception3.printStackTrace();
                                }
                            });
                        }
                    }));
        });

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
            var byteBuffer = ByteBuf.wrapForReading(bytes);
            this.socket.write(byteBuffer);
        });
    }

    @Override
    public void close() {
        this.socket.close();
        this.socket = null;
    }
}
