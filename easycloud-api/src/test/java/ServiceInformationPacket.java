import dev.easycloud.service.file.FileFactory;
import packet.ServicePacket;
import dev.easycloud.service.network.packet.SimpleService;
import dev.easycloud.service.service.Service;
import dev.httpmarco.netline.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public final class ServiceInformationPacket extends ServicePacket {
    private Service service;
    private List<Service> services;

    @Override
    public void read(PacketBuffer buffer) {
        this.service = FileFactory.GSON_NO_PRETTY.fromJson(buffer.readString(), SimpleService.class);
        this.services = FileFactory.GSON_NO_PRETTY.fromJson(buffer.readString(), ServicesList.class).services().stream().map(it -> (Service) it).toList();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(FileFactory.GSON_NO_PRETTY.toJson(new SimpleService(service)));
        buffer.writeString(FileFactory.GSON_NO_PRETTY.toJson(new ServicesList(services.stream().map(it -> new SimpleService(it)).toList())));
    }
}

record ServicesList(List<SimpleService> services) {
}
