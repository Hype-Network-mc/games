plugins {
    java
    id("com.gradleup.shadow") version "9.3.1"
}

group = "dev.emortal.minestom.battle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation(project(":gamesdk"))

    implementation("io.github.togar2:MinestomPvP:2025.12.29-1.21.11") {
        exclude(group = "net.minestom", module = "minestom")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()

        manifest {
            attributes(
                "Main-Class" to "dev.emortal.minestom.battle.Main",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build {
        dependsOn(shadowJar)
    }
}
