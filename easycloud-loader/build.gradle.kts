plugins {
    id("dev.vankka.dependencydownload.plugin") version ("1.3.1")
    id("application")
    kotlin("jvm") 
}

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")

    // define all dependencies that should be downloaded at runtime
    runtimeDownload("org.yaml:snakeyaml:2.4")
    runtimeDownload("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")

    runtimeDownload("com.google.code.gson:gson:2.11.0")
    runtimeDownload("com.google.inject:guice:7.0.0")
    //runtimeDownload("com.google.common:google-collect:1.0-rc1")

    runtimeDownload("org.jline:jline:3.30.4")
    runtimeDownload("org.fusesource.jansi:jansi:2.4.2")

    runtimeDownload("io.activej:activej:6.0-rc2")
    runtimeDownload("io.activej:activej-net:6.0-rc2")
    runtimeDownload("io.activej:activej-csp:6.0-rc2")

    runtimeDownload("org.apache.logging.log4j:log4j-core:2.24.1")
    runtimeDownload("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib:2.2.0-RC2")

    runtimeDownload("commons-io:commons-io:2.19.0")

    runtimeDownload("org.jetbrains:annotations:15.0")

    runtimeDownload(kotlin("stdlib"))
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        if(!file.name.startsWith("dependencydownload") && !file.name.startsWith("kotlin")) {
            return@forEach
        }
        from(zipTree(file.absoluteFile))
    }

    from(project(":easycloud-api").tasks.jar)
    from(project(":easycloud-cluster").tasks.jar)
    //from(project(":easycloud-service").tasks.getByPath(":easycloud-service:shadowJar"))
    from(project(":easycloud-service").tasks.jar)
    from(project(":easycloud-patcher").tasks.jar)
    from(project(":easycloud-modules:bridge-module").tasks.jar)

    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudBootKt"
    }

    archiveFileName.set("easycloud-loader.jar")
    dependsOn(
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownloadOnly"),
        tasks.named("generateRuntimeDownloadResourceForRuntimeDownload")
    )
}
/*kotlin {
    jvmToolchain(21)
}*/