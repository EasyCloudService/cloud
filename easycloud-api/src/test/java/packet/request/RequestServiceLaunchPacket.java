package packet.request;

import dev.easycloud.service.group.resources.Group;
import dev.httpmarco.netline.packet.Packet;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public final class RequestServiceLaunchPacket extends Packet {
    private String groupName;
    private int amount;

    @Override
    public void read(PacketBuffer buffer) {
        this.groupName = buffer.readString();
        this.amount = buffer.readInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(this.groupName);
        buffer.writeInt(this.amount);
    }
}
