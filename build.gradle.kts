plugins {
    id("java")
}

group = "dev.emortal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


allprojects {
    apply(plugin = "java")

    dependencies {
        // Minestom
        implementation("net.minestom:minestom:2026.01.01-1.21.11")
        implementation("net.kyori:adventure-text-minimessage:4.25.0")

        implementation("org.apache.kafka:kafka-clients:4.1.1")

        compileOnly("it.unimi.dsi:fastutil:8.5.18")
        implementation("dev.hollowcube:polar:1.15.0")
    }
}

