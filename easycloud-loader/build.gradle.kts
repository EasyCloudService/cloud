plugins {
    id("dev.vankka.dependencydownload.plugin") version ("1.3.1")
    id("application")
}

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")

    // define all dependencies that should be downloaded at runtime
    runtimeDownloadOnly("com.google.code.gson:gson:2.11.0")
    runtimeDownloadOnly("org.jline:jline:3.26.3")
    runtimeDownloadOnly("log4j:log4j:1.2.17")
    runtimeDownloadOnly("org.fusesource.jansi:jansi:2.2.0")

    runtimeDownloadOnly("dev.httpmarco.evelon:evelon-common:1.0.44-SNAPSHOT")
    runtimeDownloadOnly("dev.httpmarco.evelon:evelon-sql-h2:1.0.44-SNAPSHOT")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    from(project(":easycloud-agent").tasks.jar)
    from(project(":easycloud-api").tasks.jar)

    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudLoader"
    }

    archiveFileName.set("easycloudservice.jar")
    dependsOn(
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownloadOnly"),
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownload")
    )
}
