pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.0-RC2"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "EasyCloud"
include("easycloud-cluster")
include("easycloud-loader")
include("easycloud-api")
include("easycloud-service")
include("easycloud-patcher")
include("easycloud-modules")
include("easycloud-modules:bridge-module")
include("easycloud-modules:hub-module")