repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
    kotlin("jvm") version "2.2.0-RC2"
}

dependencies {
    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")
    compileOnly("io.activej:activej-csp:6.0-rc2")

    compileOnly("org.yaml:snakeyaml:2.4")
    compileOnly("io.activej:activej:6.0-rc2")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.jar {
    archiveFileName.set("easycloud-api.jar")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.easycloud.service"
            artifactId = "easycloud-api"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}
kotlin {
    jvmToolchain(21)
}