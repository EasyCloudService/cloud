repositories {
    mavenLocal()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
}

dependencies {
    compileOnly("org.yaml:snakeyaml:2.4")
    compileOnly("dev.httpmarco:netline:1.0.0-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("easycloud-api.jar")
}