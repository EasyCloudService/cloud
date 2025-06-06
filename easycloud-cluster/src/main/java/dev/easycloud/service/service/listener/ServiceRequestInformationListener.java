package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.ServiceInformationEvent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.LogType;
import io.activej.bytebuf.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceRequestInformationListener {

    public ServiceRequestInformationListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceRequestInformationEvent.class, (socket, event) -> {
            socket.write(ByteBuf.wrapForReading(new ServiceInformationEvent(EasyCloudCluster.instance().serviceProvider().get(event.serviceId()), EasyCloudCluster.instance().serviceProvider().services()).asBytes()));
        });
    }
}
