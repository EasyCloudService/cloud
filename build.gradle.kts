allprojects {
    apply(plugin = "java-library")

    group = "dev.easycloud.service"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        "compileOnly"("com.google.code.gson:gson:2.11.0")

        "compileOnly"("org.projectlombok:lombok:1.18.34")
        "annotationProcessor"("org.projectlombok:lombok:1.18.34")

        "testCompileOnly"("org.projectlombok:lombok:1.18.34")
        "testAnnotationProcessor"("org.projectlombok:lombok:1.18.34")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}