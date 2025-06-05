package dev.easycloud.service.network.event;

import dev.easycloud.service.network.event.resources.socket.Socket;
import io.activej.bytebuf.ByteBuf;
import io.activej.net.socket.tcp.ITcpSocket;
import lombok.Getter;

@Getter
public final class EventProvider {
    private final Socket socket;

    public EventProvider(Socket socket) {
        this.socket = socket;
        new Thread(this.socket::run).start();
    }

    public void close() {
        this.socket.close();
    }

    public void publish(Event event) {
        this.socket.write(event.serialize());
    }

    public void publishToSocket(ITcpSocket socket, Event event) {
        socket.write(ByteBuf.wrapForWriting(event.serialize().getBytes()));
    }
}
