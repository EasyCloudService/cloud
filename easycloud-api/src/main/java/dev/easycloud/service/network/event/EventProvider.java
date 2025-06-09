package dev.easycloud.service.network.event;

import dev.easycloud.service.network.socket.Socket;

public record EventProvider(Socket socket) {
    public EventProvider(Socket socket) {
        this.socket = socket;
        new Thread(this.socket::run).start();
    }

    public void close() {
        this.socket.close();
    }

    public void publish(Event event) {
        this.socket.write(event.asBytes());
    }
}
