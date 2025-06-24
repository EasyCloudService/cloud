package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudClusterOld;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.Log4jColor;
import lombok.extern.slf4j.Slf4j;


import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceReadyListener {

    public ServiceReadyListener() {
        EasyCloudClusterOld.instance().eventProvider().socket().read(ServiceReadyEvent.class, (netChannel, event) -> {
            var service = EasyCloudClusterOld.instance().serviceProvider().get(event.service().id());
            service.state(ServiceState.ONLINE);
            service.publish();

            EasyCloudClusterOld.instance().eventProvider().publish(event);
            log.info(EasyCloudClusterOld.instance().i18nProvider().get("service.ready", ansi().fgRgb(Log4jColor.WHITE.rgb()).a(service.id()).reset()));
        });
    }
}
