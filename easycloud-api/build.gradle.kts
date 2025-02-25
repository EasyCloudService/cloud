repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("dev.httpmarco:netline:1.0.0-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("easycloud-api.jar")
}