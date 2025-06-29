allprojects {
    apply(plugin = "java-library")

    group = "dev.easycloud.service"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        "compileOnly"("io.activej:activej:6.0-rc2")
        "compileOnly"("io.activej:activej-net:6.0-rc2")
        "compileOnly"("io.activej:activej-csp:6.0-rc2")

        "compileOnly"("org.github.paperspigot:paperspigot-api:1.8.8-R0.1-20160806.221350-1")
        "compileOnly"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        "annotationProcessor"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

        "compileOnly"(project(":easycloud-api"))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}