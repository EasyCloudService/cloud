package packet.proxy;

import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.net.InetSocketAddress;

@Getter
@ToString
@AllArgsConstructor
public final class UnregisterServerPacket extends Packet {
    private String id;

    @Override
    public void read(PacketBuffer buffer) {
        this.id = buffer.readString();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(this.id);
    }
}
