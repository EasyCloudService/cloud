package dev.easycloud.service.network.event;

import com.google.gson.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event {
    private static Gson tmpGson;
    private static final GsonBuilder builder = new GsonBuilder();

    private final String eventId = getClass().getName();

    public static <E, T> void registerTypeAdapter(
        Class<E> clazz,
        Class<T> typeAdapterClass
    ) {
        builder.registerTypeAdapter(
            clazz,
            (JsonDeserializer<E>) (json, typeOfT, context) ->
                context.deserialize(json, typeAdapterClass)
        );
    }

    public static Gson gson() {
        if (tmpGson == null) {
            tmpGson = builder.create();
        }
        return tmpGson;
    }

    public static Event deserialize(
        String json,
        Class<? extends Event> eventClass
    ) {
        try {
            return gson().fromJson(json, eventClass);
        } catch (Exception e) {
            log.info("Deserializing {}: {}", eventClass, json);
            throw new RuntimeException(e);
        }
    }

    public String serialize() {
        return gson().toJson(this);
    }

    public byte[] asBytes() {
        return this.serialize().getBytes();
    }
}
