package dev.easycloud.service.service.listener;

import dev.easycloud.service.EasyCloudAgent;
import dev.easycloud.service.network.event.resources.ServiceReadyEvent;
import dev.easycloud.service.service.SimpleService;
import dev.easycloud.service.service.resources.ServiceState;
import dev.easycloud.service.terminal.logger.LogType;
import lombok.extern.slf4j.Slf4j;


import static org.jline.jansi.Ansi.ansi;

@Slf4j
public final class ServiceReadyListener {

    public ServiceReadyListener() {
        EasyCloudAgent.instance().eventProvider().subscribe(ServiceReadyEvent.class, (netChannel, event) -> {
            var service = (SimpleService) event.service();
            service.state(ServiceState.ONLINE);
            EasyCloudAgent.instance().eventProvider().publish(event);
            log.info(EasyCloudAgent.instance().i18nProvider().get("service.ready", ansi().fgRgb(LogType.WHITE.rgb()).a(service.id()).reset()));
        });
    }
}
