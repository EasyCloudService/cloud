package dev.easycloud.service.module.resources;

import dev.easycloud.service.EasyCloudCluster;
import dev.easycloud.service.module.Module;
import dev.easycloud.service.module.ModuleLoader;
import dev.easycloud.service.platform.PlatformModule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModuleLoaderImpl implements ModuleLoader {
    private final Map<String, List<Class<? extends Module>>> modules = new HashMap<>();

    @Override
    public void clear() {
        this.modules.clear();
        EasyCloudCluster.instance().platformProvider().initializers().forEach(it -> this.modules.put(it.id(), new ArrayList<>()));
    }

    @Override
    public void add(Class<? extends Module> moduleClass) {
        var platformModule = moduleClass.getAnnotation(PlatformModule.class);
        if (platformModule == null) {
            log.warn("Class {} is not annotated with @Platform, skipping.", moduleClass.getName());
            return;
        }

        var platform = EasyCloudCluster.instance().platformProvider().initializer(platformModule.platformId());
        this.modules.get(platform.id()).add(moduleClass);

        log.info("Added module: {} for platform: {}", moduleClass.getName(), platform.id());
    }

    @Override
    public void load(Module module) {
        log.info("Loading module: {} (version: {})", module.getClass().getSimpleName(), module.getClass().getPackage().getImplementationVersion());
        module.onLoad();
    }
}
