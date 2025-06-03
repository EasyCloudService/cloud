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
    implementation(project(":easycloud-api"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")

    implementation("io.netty:netty5-all:5.0.0.Alpha5")
    implementation("dev.httpmarco:netline:1.0.0-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:15.0")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("easycloud-plugin.jar")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.easycloud.service"
            artifactId = "easycloud-plugin"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}