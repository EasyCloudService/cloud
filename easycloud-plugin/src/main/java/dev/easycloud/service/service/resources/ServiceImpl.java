package dev.easycloud.service.service.resources;

import dev.easycloud.service.group.resources.Group;
import dev.easycloud.service.service.ExtendedService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public final class ServiceImpl implements ExtendedService {
    private final String id;
    private final Group group;

    private ServiceState state;

    private final int port;
    private final String directoryRaw;

    @Override
    public void publish() {

    }

   /*@Override
   public <T> Map<T, Object> properties() {
      return Map.of();
   }*/
}
