plugins {
    `java-library`
}

group = "io.github.diamondpixel"
version = "3.0.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:6.0.58")
    compileOnly("com.mojang:brigadier:1.3.10")
}
