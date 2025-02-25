import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("com.gradleup.shadow") version ("9.0.0-beta8")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("org.jline:jline:3.26.3")
    compileOnly("org.fusesource.jansi:jansi:2.2.0")

    compileOnly("io.netty:netty5-all:5.0.0.Alpha5")
    compileOnly("dev.httpmarco:netline:1.0.0-SNAPSHOT")

    compileOnly("org.apache.logging.log4j:log4j-core:2.24.1")
    compileOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    compileOnly("org.jetbrains:annotations:15.0")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("easycloud-agent.jar")
}