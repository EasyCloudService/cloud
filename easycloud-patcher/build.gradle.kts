plugins {
    id("java")
}

group = "dev.easycloud.service"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudPatcher"
    }

    archiveFileName.set("easycloud-patcher.jar")
}