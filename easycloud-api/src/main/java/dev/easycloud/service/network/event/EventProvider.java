package dev.easycloud.service.network.event;

import dev.easycloud.service.network.event.resources.socket.Socket;
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
}
