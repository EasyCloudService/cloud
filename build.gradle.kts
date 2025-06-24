allprojects {
    apply(plugin = "java-library")

    group = "dev.easycloud.service"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        "compileOnly"("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")

        "compileOnly"("com.google.code.gson:gson:2.12.1")
        "compileOnly"("com.google.inject:guice:7.0.0")

        "compileOnly"("org.projectlombok:lombok:1.18.36")
        "annotationProcessor"("org.projectlombok:lombok:1.18.36")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}