package dev.easycloud.service.network.event;

import com.google.gson.*;
import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event extends Packet {
    private transient Event deserialized;

    @Override
    public void read(PacketBuffer packetBuffer) {
        this.deserialized = Event.deserialize(packetBuffer.readString(), this.getClass());
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.writeString(this.serialize());
    }

    private static Gson tmpGson;
    private final static GsonBuilder builder = new GsonBuilder();
    public static <E, T> void registerTypeAdapter(Class<E> clazz, Class<T> typeAdapterClass) {
        builder.registerTypeAdapter(clazz, (JsonDeserializer<E>) (json, typeOfT, context) -> {
            return context.deserialize(json, typeAdapterClass);
        });
    }

    public static Gson gson() {
        if(tmpGson == null) {
            tmpGson = builder.create();
        }
        return tmpGson;
    }


    public static Event deserialize(String json, Class<? extends Event> eventClass) {
        return gson().fromJson(json, eventClass);
    }

    public String serialize() {
        return gson().toJson(this);
    }
}