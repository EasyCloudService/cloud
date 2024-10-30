repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("org.jline:jline:3.26.3")
    compileOnly("org.fusesource.jansi:jansi:2.2.0")

    compileOnly("org.apache.logging.log4j:log4j-core:2.24.1")
    compileOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    compileOnly("org.jetbrains:annotations:15.0")
}

tasks.jar {
    archiveFileName.set("easycloud-agent.jar")
}