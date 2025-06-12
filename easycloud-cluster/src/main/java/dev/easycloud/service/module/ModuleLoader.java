package dev.easycloud.service.module;

import dev.easycloud.service.module.resources.ModuleLoaderImpl;

public interface ModuleLoader {
    void clear();
    void add(Class<? extends Module> moduleClass);
    void load(Module module);


    ModuleLoaderImpl simple = new ModuleLoaderImpl();
    static ModuleLoader simple() {
        return simple;
    }
}
