repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("com.google.code.gson:gson:2.11.0")
}

tasks.jar {
    archiveFileName.set("easycloud-agent.jar")
}