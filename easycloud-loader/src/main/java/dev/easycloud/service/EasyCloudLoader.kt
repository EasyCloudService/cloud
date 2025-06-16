package dev.easycloud.service

import dev.easycloud.service.dependency.DependencyLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.system.exitProcess

class EasyCloudLoader {

    fun load() {
        val localPath = Paths.get("local")
        val resourcesPath = Paths.get("resources")
        val librariesPath = resourcesPath.resolve("libs")
        val modulesPath = Paths.get("modules")

        // Create directories if they do not exist
        localPath.takeIf { !it.exists() }?.createDirectory()
        resourcesPath.takeIf { !it.exists() }?.createDirectory()
        librariesPath.takeIf { !it.exists() }?.createDirectory()
        modulesPath.takeIf { !it.exists() }?.createDirectory()

        // Check if the loader-patcher.jar file exists
        if (Files.exists(Paths.get("loader-patcher.jar"))) {
            println("Patching could have been failed.")
            Files.delete(Paths.get("loader-patcher.jar"))
        }

        // Load the EasyCloudCluster
        mapOf(
            Pair("easycloud-patcher", librariesPath.resolve("dev.easycloud.patcher")),
            Pair("easycloud-service", resourcesPath.resolve("easycloud-service")),
            Pair("easycloud-api", librariesPath.resolve("dev.easycloud.api")),
            Pair("easycloud-cluster", Paths.get("easycloud-cluster")),
            Pair("bridge-module", modulesPath.resolve("bridge-module")),
        ).forEach {
            this.copyFile("${it.key}.jar", Paths.get("${it.value}.jar"))
        }

        DependencyLoader().load(librariesPath)

        val thread = Thread {
            try {
                var fileArg = "easycloud-cluster.jar;resources/libs/*;"
                if (!System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) {
                    fileArg = fileArg.replace(";", ":")
                }
                val process = ProcessBuilder(
                    "java",
                    "-Xms512M",
                    "-Xmx512M",
                    "--enable-native-access=ALL-UNNAMED",
                    "-cp",
                    fileArg,
                    "dev.easycloud.service.EasyCloudBoot"
                ).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .start()

                process.waitFor()
            } catch (exception: IOException) {
                throw RuntimeException(exception)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                exitProcess(1)
            }
        }
        thread.setDaemon(false)
        thread.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            if (thread.isAlive) thread.interrupt()
        })

        while (true) {
            if (!thread.isAlive) exitProcess(0)
        }
    }

    private fun copyFile(name: String, destination: Path) {
        val file = ClassLoader.getSystemClassLoader().getResourceAsStream(name)
            ?: throw IllegalArgumentException("Resource $name not found")
        Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING)
    }
}