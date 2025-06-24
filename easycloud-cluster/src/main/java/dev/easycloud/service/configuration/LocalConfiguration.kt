package dev.easycloud.service.configuration

import dev.easycloud.service.configuration.resources.ConfigurationEntity
import java.util.Locale

@ConfigurationEntity(name = "local")
class LocalConfiguration(
    var language: Locale = Locale.ENGLISH,
    var announceUpdates: Boolean = true,
    var clusterPort: Int = 8080,
    var proxyPort: Int = 25565,
    var startingSameTime: Int = 3,
    var dynamicPercentage: Int = 80
)