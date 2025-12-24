plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

group = "io.github.diamondpixel"
version = "2.0.4-SNAPSHOT"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(project(":SkullCreatorAPI"))
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    archiveClassifier.set("") // produce plugin jar with API inside
}

tasks.build { dependsOn(tasks.shadowJar) }
