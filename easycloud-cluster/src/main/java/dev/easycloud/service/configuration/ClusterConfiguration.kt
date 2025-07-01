package dev.easycloud.service.configuration

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

class ClusterConfiguration {
    val path: Path = Paths.get("resources").resolve("config")

    lateinit var local: LocalConfiguration
    lateinit var security: SecurityConfiguration

    fun load() {
        path.takeIf { !it.exists() }?.createDirectory()

        Configurations.writeIfNotExists(path, LocalConfiguration())
        Configurations.writeIfNotExists(path, SecurityConfiguration())
    }

    fun reload() {
        local = Configurations.read(path, LocalConfiguration::class.java)
        security = Configurations.read(path, SecurityConfiguration::class.java)
    }

    fun publish(config: Any) {
        when (config) {
            is LocalConfiguration -> {
                local = config
                Configurations.write(path, local)
            }

            is SecurityConfiguration -> {
                security = config
                Configurations.write(path, security)
            }

            else -> {
                throw IllegalArgumentException("Unsupported configuration type: ${config::class.java.name}")
            }
        }
    }
}