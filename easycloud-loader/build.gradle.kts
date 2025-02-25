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
    runtimeDownload("com.google.code.gson:gson:2.11.0")
    runtimeDownload("org.jline:jline:3.26.3")
    runtimeDownload("org.fusesource.jansi:jansi:2.2.0")

    runtimeDownload("dev.httpmarco:netline:1.0.0-SNAPSHOT")
    runtimeDownload("io.netty:netty5-all:5.0.0.Alpha5")

    runtimeDownload("org.apache.logging.log4j:log4j-core:2.24.1")
    runtimeDownload("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    runtimeDownload("org.jetbrains:annotations:15.0")

}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }

    from(project(":easycloud-agent").tasks.getByPath(":easycloud-agent:shadowJar"))
    from(project(":easycloud-plugin").tasks.getByPath(":easycloud-plugin:shadowJar"))

    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudLoader"
    }

    archiveFileName.set("easycloudservice.jar")
    dependsOn(
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownloadOnly"),
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownload")
    )
}
