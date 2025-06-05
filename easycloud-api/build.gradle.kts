repositories {
    mavenLocal()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
}

dependencies {
    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")

    compileOnly("org.yaml:snakeyaml:2.4")
    compileOnly("io.activej:activej:6.0-rc2")
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