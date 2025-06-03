package dev.easycloud.service.network.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event extends Packet {
    private final static ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private final String eventId = this.getClass().getSimpleName();
    private transient Event deserializedEvent = null;

    @Override
    public void read(PacketBuffer packetBuffer) {
        var json = packetBuffer.readString();
        System.out.println(json);

        System.out.println("READ");

        System.out.println("deserializedEvent: " + Event.deserialize(json, getClass()));
        System.out.println("READ 2");
        this.deserializedEvent = Event.deserialize(json, this.getClass());
        System.out.println("deserialized!");
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        System.out.println(this.serialize());

        packetBuffer.writeString(this.serialize());
    }


    private static final Map<Class<?>, Class<?>> typeAdapters = new HashMap<>();
    public static <E, T> void registerTypeAdapter(Class<E> eventClass, Class<T> typeAdapterClass) {
        // add type adapter to object mapper
        objectMapper.registerModule(new SimpleModule()
                .addDeserializer(eventClass, new JsonDeserializer() {
                    @Override
                    public E deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                        return (E) jsonParser.readValueAs(typeAdapterClass);
                    }
                }));
        /*objectMapper.registerSubtypes(typeAdapterClass);

        if (typeAdapters.containsKey(eventClass)) {
            throw new IllegalArgumentException("Type adapter for " + eventClass.getName() + " is already registered.");
        }
        typeAdapters.put(eventClass, typeAdapterClass);*/
    }


    public static Event deserialize(String json, Class<? extends Event> eventClass) {

        try {
            return objectMapper.readValue(json, eventClass);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public String serialize() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}