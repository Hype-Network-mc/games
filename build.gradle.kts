plugins {
    id("java")
}

group = "dev.emortal"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")

    repositories {
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            content { // This filtering is optional, but recommended
                includeModule("net.minestom", "minestom")
                includeModule("net.minestom", "testing")
            }
        }
        mavenCentral()
    }

    dependencies {
        // Minestom
        implementation("net.minestom:minestom:26_1-SNAPSHOT")
        implementation("net.kyori:adventure-text-minimessage:4.25.0")

        implementation("org.apache.kafka:kafka-clients:4.1.1")

        compileOnly("it.unimi.dsi:fastutil:8.5.18")
        implementation("dev.hollowcube:polar:1.15.0")
    }
}

