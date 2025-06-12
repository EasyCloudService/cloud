repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

tasks.withType<Jar> {
    archiveFileName.set("bridge-module.jar")
}
