package dev.easycloud.service.service;

import dev.easycloud.service.service.resources.Service;

public interface AdvancedServiceProvider extends ServiceProvider {
    Service thisService();
}
