package packet;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.Service;
import dev.easycloud.service.service.resources.ServiceState;
import dev.httpmarco.netline.packet.Packet;
import lombok.Getter;

public abstract class ServicePacket extends Packet {
}

@Getter
class SimpleService implements Service {
    private final String id;
    private final Group group;

    private ServiceState state;

    private final int port;
    private final String directoryRaw;

    public SimpleService(Service service) {
        this.id = service.id();
        this.group = service.group();
        this.state = service.state();
        this.port = service.port();
        this.directoryRaw = service.directoryRaw();
    }
}