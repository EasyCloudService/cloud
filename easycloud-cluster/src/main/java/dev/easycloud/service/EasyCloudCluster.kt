package dev.easycloud.service

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import dev.easycloud.service.command.CommandProvider
import dev.easycloud.service.configuration.ClusterConfiguration
import dev.easycloud.service.files.EasyFiles
import dev.easycloud.service.group.GroupProvider
import dev.easycloud.service.group.GroupProviderImpl
import dev.easycloud.service.i18n.I18nProvider
import dev.easycloud.service.module.ModuleService
import dev.easycloud.service.network.event.Event
import dev.easycloud.service.network.event.EventProvider
import dev.easycloud.service.network.socket.ServerSocket
import dev.easycloud.service.onboarding.OnboardingProvider
import dev.easycloud.service.platform.PlatformProvider
import dev.easycloud.service.release.ReleasesService
import dev.easycloud.service.service.Service
import dev.easycloud.service.service.ServiceImpl
import dev.easycloud.service.service.ServiceProvider
import dev.easycloud.service.service.ServiceProviderImpl
import dev.easycloud.service.terminal.Terminal
import dev.easycloud.service.terminal.TerminalImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths

class EasyCloudCluster {
    val logger: Logger = LoggerFactory.getLogger(EasyCloudCluster::class.java)
    val startedAt = System.currentTimeMillis()

    lateinit var injector: Injector

    fun load() {
        // Set paths
        val localPath = Paths.get("local")
        val resourcePath = Paths.get("resources")
        EasyFiles.remove(localPath.resolve("dynamic"))

        // Initialize google guice injector
        injector = Guice.createInjector(object : AbstractModule() {
            override fun configure() {
                // Save cluster configuration
                val clusterConfiguration = ClusterConfiguration()
                clusterConfiguration.load()
                clusterConfiguration.reload()
                bind(ClusterConfiguration::class.java)
                    .toInstance(clusterConfiguration)

                // Bind services and providers
                bind(I18nProvider::class.java).asEagerSingleton()
                bind(Terminal::class.java)
                    .to(TerminalImpl::class.java)
                    .asEagerSingleton()

                val socket = ServerSocket(clusterConfiguration.security.value, clusterConfiguration.local.clusterPort)
                bind(EventProvider::class.java).toInstance(EventProvider(socket))

                bind(ServiceProvider::class.java)
                    .to(ServiceProviderImpl::class.java)
                    .asEagerSingleton()
                bind(CommandProvider::class.java).asEagerSingleton()

                bind(GroupProvider::class.java)
                    .to(GroupProviderImpl::class.java)
                    .asEagerSingleton()
                bind(PlatformProvider::class.java).asEagerSingleton()
                bind(ModuleService::class.java).asEagerSingleton()
                bind(ReleasesService::class.java).asEagerSingleton()
            }
        })
    }

    fun run() {
        // Run terminal
        injector.getInstance(TerminalImpl::class.java).run()

        // Check if property is set for first start
        if (System.getProperty("easycloud.first-start") == "true") {
            OnboardingProvider().run()
        }

        // Configuration
        val configuration = injector.getInstance(ClusterConfiguration::class.java)

        // TODO: temporary fix for old cluster
        EasyCloudClusterOld(injector)
        // TODO: temporary fix for old cluster


        val i18nProvider = injector.getInstance(I18nProvider::class.java)
        logger.info(
            i18nProvider.get(
                "net.listening",
                "<white>0.0.0.0<reset>",
                "<white>${configuration.local.clusterPort}<reset>"
            )
        );

        // Run eventProvider
        val eventProvider = injector.getInstance(EventProvider::class.java)
        eventProvider.run()
        eventProvider.socket.waitForConnection()

        // Register event types
        Event.registerTypeAdapter(Service::class.java, ServiceImpl::class.java)

        // Search platforms
        val platformProvider = injector.getInstance(PlatformProvider::class.java)
        platformProvider.search()

        val platformTypes = StringBuilder()
        platformProvider.initializers().forEach { platform ->
            platformTypes.append("<white>${platform.id()}<reset>, ")
        }
        logger.info(i18nProvider.get("cluster.found", "<white>platforms<reset>", platformTypes))

        // Search groups
        val groupProvider = injector.getInstance(GroupProvider::class.java)
        groupProvider.search()

        val groups = StringBuilder()
        groupProvider.groups().forEach { group ->
            groups.append("<white>${group.name.lowercase()}<reset>, ")
        }
        logger.info(i18nProvider.get("cluster.found", "<white>groups<reset>", groups))

        // Search modules
        injector.getInstance(ModuleService::class.java).search()

        // Cluster ready
        logger.info(i18nProvider.get("cluster.ready", "<white>${System.currentTimeMillis() - startedAt}ms<reset>"))
    }
}