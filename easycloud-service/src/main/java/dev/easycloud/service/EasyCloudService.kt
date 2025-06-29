package dev.easycloud.service

import dev.easycloud.service.network.event.Event
import dev.easycloud.service.network.event.EventProvider
import dev.easycloud.service.service.listener.ServiceUpdateListener
import dev.easycloud.service.network.event.resources.ServiceInformationEvent
import dev.easycloud.service.network.event.resources.ServiceReadyEvent
import dev.easycloud.service.network.event.resources.ServiceShutdownEvent
import dev.easycloud.service.network.event.resources.request.ServiceRequestInformationEvent
import dev.easycloud.service.network.socket.ClientSocket
import dev.easycloud.service.service.Service
import dev.easycloud.service.service.ServiceProvider
import dev.easycloud.service.service.resources.ServiceImpl
import dev.easycloud.service.service.resources.ServiceProviderImpl
import io.activej.inject.Injector
import io.activej.inject.module.ModuleBuilder
import io.activej.net.socket.tcp.ITcpSocket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

class EasyCloudService(val key: String, val serviceId: String, val port: Int) {
    val logger: Logger = LoggerFactory.getLogger(EasyCloudService::class.java)
    var thisService: Service? = null

    companion object {
        lateinit var injector: Injector
    }

    fun load() {
        val socket = ClientSocket(key, port)
        val eventProvider = EventProvider(socket)
        eventProvider.run()
        eventProvider.socket().waitForConnection().get()

        val serviceProvider = ServiceProviderImpl(eventProvider)
        ServiceImpl.eventProvider = eventProvider

        Event.registerTypeAdapter(Service::class.java, ServiceImpl::class.java)
        Thread {
            eventProvider.socket.read(ServiceInformationEvent::class.java) { channel: ITcpSocket?, event: ServiceInformationEvent ->
                event.services()
                    .forEach(Consumer { service: Service -> serviceProvider.services().add(service) })
                thisService = event.service()
            }
        }.start()

        // Request service information
        eventProvider.publish(ServiceRequestInformationEvent(serviceId))

        while (thisService == null) {
            Thread.sleep(100)
        }

        val moduleBuilder = ModuleBuilder.create()
        // Bind services and providers
        moduleBuilder.bind(EventProvider::class.java).toInstance(eventProvider)
        moduleBuilder.bind(ServiceProvider::class.java).toInstance(serviceProvider)
        moduleBuilder.bind(Service::class.java).toInstance(thisService!!)
        // Bind listeners
        moduleBuilder.bind(ServiceUpdateListener::class.java)

        // Inject
        injector = Injector.of(moduleBuilder.build())
        CloudInjector.initialize(injector)

        // Loaded
        logger.info("Welcome back, @${thisService!!.id()}")
        EasyCloudServiceBoot.loaded = true
    }

    fun run() {
        val eventProvider = injector.getInstance(EventProvider::class.java)
        val serviceProvider = injector.getInstance(ServiceProvider::class.java)

        (serviceProvider as ServiceProviderImpl).init(injector)

        eventProvider.socket.read(ServiceReadyEvent::class.java) { channel: ITcpSocket?, event: ServiceReadyEvent ->
            if (event.service().id() == thisService!!.id()) return@read
            serviceProvider.services().add(event.service())
            logger.info("Service '{}' has connected.", event.service().id())
        }

        eventProvider.socket.read(ServiceShutdownEvent::class.java) { channel: ITcpSocket?, event: ServiceShutdownEvent ->
            if (event.service().id() == thisService!!.id()) return@read
            serviceProvider.services().removeIf({ it -> it.id().equals(event.service().id()) })
            logger.info("Service '{}' has been shut down.", event.service().id())
        }
    }
}