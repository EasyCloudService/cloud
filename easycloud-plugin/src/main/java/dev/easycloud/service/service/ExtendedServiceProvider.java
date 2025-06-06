package dev.easycloud.service.service;

import dev.easycloud.service.service.resources.Service;

public interface ExtendedServiceProvider extends ServiceProvider {
    Service thisService();
}
