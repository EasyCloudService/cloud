repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":easycloud-api"))

    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")
    compileOnly("io.activej:activej-csp:6.0-rc2")

    compileOnly("org.apache.logging.log4j:log4j-api:2.25.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.25.0")
}

tasks.withType<Jar> {
    archiveFileName.set("easycloud-service.jar")
    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudServiceBoot"
        attributes["Premain-Class"] = "dev.easycloud.service.EasyCloudServiceBoot"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.easycloud.service"
            artifactId = "easycloud-service"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}