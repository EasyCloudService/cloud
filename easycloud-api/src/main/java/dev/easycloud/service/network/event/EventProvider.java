package dev.easycloud.service.network.event;

import dev.httpmarco.netline.NetChannel;
import dev.httpmarco.netline.client.NetClient;
import dev.httpmarco.netline.server.NetServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EventProvider {
    private final NetClient client;
    private final NetServer server;

    private final Map<Class<? extends Event>, List<BiConsumer<NetChannel, Event>>> eventHandlers = new HashMap<>();

    public EventProvider(NetClient client) {
        this.client = client;
        this.server = null;
    }

    public EventProvider(NetServer server) {
        this.client = null;
        this.server = server;
    }

    public <T extends Event> void subscribe(Class<T> event, BiConsumer<NetChannel, T> onEvent) {
        if (!this.eventHandlers.containsKey(event)) {
            this.eventHandlers.put(event, new ArrayList<>());
        }
        this.eventHandlers.get(event).add((BiConsumer<NetChannel, Event>) onEvent);
        if(this.client != null) {
            this.client.track(event, (netChannel, t) -> {
                this.eventHandlers.get(event).forEach(it -> it.accept(netChannel, t.deserialized()));
            });
        } else if (this.server != null) {
            this.server.track(event, (netChannel, t) -> {
                this.eventHandlers.get(event).forEach(it -> it.accept(netChannel, t.deserialized()));
            });
        } else {
            throw new IllegalStateException("EventProvider is not initialized with a client or server.");
        }
    }

    public void close() {
        if (this.client != null) {
            this.client.closeSync();
        } else if (this.server != null) {
            this.server.closeSync();
        } else {
            throw new IllegalStateException("EventProvider is not initialized with a client or server.");
        }
    }

    public void publish(Event event) {
        if(this.client != null) {
            this.client.send(event);
        } else if (this.server != null) {
            this.server.broadcast(event);
        } else {
            throw new IllegalStateException("EventProvider is not initialized with a client or server.");
        }
    }
}
