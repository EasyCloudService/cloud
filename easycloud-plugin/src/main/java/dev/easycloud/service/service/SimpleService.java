package dev.easycloud.service.service;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.resources.Service;
import dev.easycloud.service.service.resources.ServiceState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class SimpleService implements Service {
    private final String id;
    private final Group group;

    private ServiceState state;

    private final int port;
    private final String directoryRaw;

   /*@Override
   public <T> Map<T, Object> properties() {
      return Map.of();
   }*/
}
