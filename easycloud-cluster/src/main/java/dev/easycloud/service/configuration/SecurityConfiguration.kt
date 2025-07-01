package dev.easycloud.service.configuration

import dev.easycloud.service.configuration.resources.ConfigurationEntity
import java.util.concurrent.ThreadLocalRandom

@ConfigurationEntity(name = "security")
class SecurityConfiguration(val value: String = "easyCloud" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000000, 99999999))