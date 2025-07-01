repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":easycloud-api"))

    compileOnly("org.yaml:snakeyaml:2.4")

    compileOnly("org.jline:jline:3.30.4")
    compileOnly("org.fusesource.jansi:jansi:2.4.2")

    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")
    compileOnly("io.activej:activej-csp:6.0-rc2")

    compileOnly("org.apache.logging.log4j:log4j-core:2.24.1")
    compileOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    compileOnly("org.jetbrains:annotations:15.0")

    compileOnly("commons-io:commons-io:2.19.0")
}


tasks.jar {
    archiveFileName.set("easycloud-cluster.jar")
}