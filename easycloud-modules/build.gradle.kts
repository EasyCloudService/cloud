allprojects {
    apply(plugin = "java-library")

    group = "dev.easycloud.service"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        "compileOnly"("io.activej:activej:6.0-rc2")
        "compileOnly"("io.activej:activej-net:6.0-rc2")
        "compileOnly"("io.activej:activej-csp:6.0-rc2")

        "compileOnly"("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
        "compileOnly"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
        "annotationProcessor"("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

        "compileOnly"(project(":easycloud-api"))
        "compileOnly"(project(":easycloud-service"))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}