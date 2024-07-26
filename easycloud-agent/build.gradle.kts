repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("org.jline:jline:3.26.3")
    compileOnly("log4j:log4j:1.2.17")
    compileOnly("org.fusesource.jansi:jansi:2.2.0")
}

tasks.jar {
    archiveFileName.set("easycloud-agent.jar")
}