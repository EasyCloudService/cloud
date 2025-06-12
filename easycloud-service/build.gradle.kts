import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
    id("com.gradleup.shadow") version ("9.0.0-beta8")
}

dependencies {
    compileOnly(project(":easycloud-api"))

    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")
    compileOnly("io.activej:activej-csp:6.0-rc2")

    //implementation("ch.qos.logback:logback-classic:1.5.18")
    //implementation("ch.qos.logback:logback-core:1.5.18")

    implementation("org.apache.logging.log4j:log4j-core:2.24.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")
}

tasks.withType<ShadowJar> {
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