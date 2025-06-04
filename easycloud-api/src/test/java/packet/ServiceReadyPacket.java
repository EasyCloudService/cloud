package packet;

import dev.easycloud.service.file.FileFactory;
import dev.easycloud.service.service.resources.Service;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public final class ServiceReadyPacket extends ServicePacket {
    private Service service;

    @Override
    public void read(PacketBuffer buffer) {
        this.service = FileFactory.GSON_NO_PRETTY.fromJson(buffer.readString(), SimpleService.class);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(FileFactory.GSON_NO_PRETTY.toJson(new SimpleService(service)));
    }
}
