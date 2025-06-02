package dev.easycloud.service.network.event;

import dev.easycloud.service.file.FileFactory;
import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class Event extends Packet {
    private Event deserializedEvent = null;

    @Override
    public void read(PacketBuffer packetBuffer) {
        this.deserializedEvent = Event.deserialize(packetBuffer.readString(), this.getClass());
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.writeString(this.serialize());
    }

    public static Event deserialize(String json, Class<? extends Event> eventClass) {
        return FileFactory.GSON_NO_PRETTY.fromJson(json, eventClass);
    }

    public String serialize() {
        return FileFactory.GSON_NO_PRETTY.toJson(this);
    }
}
