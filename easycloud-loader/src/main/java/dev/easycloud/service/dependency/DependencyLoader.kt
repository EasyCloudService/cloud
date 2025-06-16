package dev.easycloud.service.dependency

import dev.vankka.dependencydownload.DependencyManager
import dev.vankka.dependencydownload.repository.Repository
import dev.vankka.dependencydownload.repository.StandardRepository
import java.nio.file.Path
import java.util.concurrent.Executors

class DependencyLoader {

    fun load(librariesPath: Path) {
        val executor = Executors.newCachedThreadPool()
        val manager = DependencyManager(librariesPath)

        manager.loadFromResource(ClassLoader.getSystemClassLoader().getResource("runtimeDownloadOnly.txt") ?:
            throw IllegalStateException("Could not find runtimeDownloadOnly.txt in resources."))
        manager.downloadAll(
            executor, listOf<Repository?>(
                StandardRepository("https://repo1.maven.org/maven2/"),
                StandardRepository("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            )
        ).join()
    }
}