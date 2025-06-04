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
public final class RegisterServerPacket extends Packet {
    private String id;
    private InetSocketAddress address;

    @Override
    public void read(PacketBuffer buffer) {
        this.id = buffer.readString();
        this.address = new InetSocketAddress(buffer.readInt());
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(this.id);
        buffer.writeInt(this.address.getPort());
    }
}
