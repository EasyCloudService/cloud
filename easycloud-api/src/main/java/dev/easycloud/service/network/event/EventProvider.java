package dev.easycloud.service.network.event;

import dev.easycloud.service.network.socket.Socket;

public record EventProvider(Socket socket) {
    public EventProvider(Socket socket) {
        this.socket = socket;
        var thread = new Thread(this.socket::run);
        thread.setName("EventProvider" + socket.getClass().getSimpleName());
        thread.start();
    }

    public void close() {
        this.socket.close();
    }

    public void publish(Event event) {
        this.socket.write(event.asBytes());
    }
}
