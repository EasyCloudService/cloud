package dev.easycloud.service.network.packet;

import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public final class ServiceConnectPacket extends Packet {
    private String serviceId;

    @Override
    public void read(PacketBuffer buffer) {
        this.serviceId = buffer.readString();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(this.serviceId);
    }
}
