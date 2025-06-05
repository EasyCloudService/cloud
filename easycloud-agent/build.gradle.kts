repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly(project(":easycloud-api"))

    compileOnly("org.yaml:snakeyaml:2.4")

    compileOnly("org.jline:jline:3.26.3")
    compileOnly("org.fusesource.jansi:jansi:2.2.0")

    //compileOnly("io.netty:netty5-all:5.0.0.Alpha5")
    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")

    compileOnly("org.apache.logging.log4j:log4j-core:2.24.1")
    compileOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    compileOnly("org.jetbrains:annotations:15.0")
}


tasks.jar {
    archiveFileName.set("easycloud-agent.jar")
}