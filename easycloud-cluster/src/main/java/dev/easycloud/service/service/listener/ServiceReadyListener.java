package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.service.ServiceImpl;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;


import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceReadyListener {

    public ServiceReadyListener() {
        EasyCloudCluster.instance().eventProvider().socket().read(ServiceReadyEvent.class, (netChannel, event) -> {
            var service = EasyCloudCluster.instance().serviceProvider().get(event.service().id());
            service.state(ServiceState.ONLINE);
            service.publish();

            EasyCloudCluster.instance().eventProvider().publish(event);
            log.info(EasyCloudCluster.instance().i18nProvider().get("service.ready", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset()));
        });
    }
}
