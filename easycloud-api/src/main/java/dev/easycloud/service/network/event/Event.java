package dev.easycloud.service.network.event;

import com.google.gson.*;
import io.activej.bytebuf.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event {
    private static Gson tmpGson;
    private final static GsonBuilder builder = new GsonBuilder();

    private final String eventId = getClass().getName();

    public static <E, T> void registerTypeAdapter(Class<E> clazz, Class<T> typeAdapterClass) {
        builder.registerTypeAdapter(clazz, (JsonDeserializer<E>) (json, typeOfT, context) -> context.deserialize(json, typeAdapterClass));
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

    public byte[] asBytes() {
        return this.serialize().getBytes();
    }
}