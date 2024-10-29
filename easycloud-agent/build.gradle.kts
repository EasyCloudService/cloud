repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("org.jline:jline:3.26.3")
    compileOnly("log4j:log4j:1.2.17")
    compileOnly("org.fusesource.jansi:jansi:2.2.0")

    compileOnly("dev.httpmarco.evelon:evelon-common:1.0.46-SNAPSHOT")
    compileOnly("dev.httpmarco.evelon:evelon-sql-h2:1.0.46-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("easycloud-agent.jar")
}