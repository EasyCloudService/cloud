package dev.easycloud.service.network.packet;

import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public final class ServiceReadyPacket extends Packet {
    private String serviceId;
    private int port;

    @Override
    public void read(PacketBuffer buffer) {
        this.serviceId = buffer.readString();
        this.port = buffer.readInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(this.serviceId);
        buffer.writeInt(this.port);
    }
}
