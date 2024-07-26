plugins {
    id("dev.vankka.dependencydownload.plugin") version ("1.3.1")
}

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")

    // define all dependencies that should be downloaded at runtime
    runtimeDownloadOnly("com.google.code.gson:gson:2.11.0")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    from(project(":easycloud-agent").tasks.jar)

    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudLoader"
    }

    archiveFileName.set("easycloudservice.jar")
    dependsOn(
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownloadOnly"),
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownload")
    )
}