//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

plugins {
    id("maven-publish")
    //id("com.gradleup.shadow") version ("9.0.0-beta8")
}

dependencies {
    implementation(project(":easycloud-api"))

    compileOnly("io.activej:activej:6.0-rc2")
    compileOnly("io.activej:activej-net:6.0-rc2")
    compileOnly("io.activej:activej-csp:6.0-rc2")
}

tasks.withType<Jar> {
    archiveFileName.set("easycloud-service.jar")
    manifest {
        attributes["Main-Class"] = "dev.easycloud.service.EasyCloudServiceBootstrap"
        attributes["Premain-Class"] = "dev.easycloud.service.EasyCloudServiceBootstrap"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.easycloud.service"
            artifactId = "easycloud-service"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}