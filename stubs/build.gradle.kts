plugins {
    id("java")
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.9.1"
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven { url = uri("https://libraries.minecraft.net/") }
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    gradlePluginPortal()
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21")
    mappings("net.fabricmc:yarn:1.21+build.9:v2")
//    modImplementation("net.fabricmc:fabric-api:0.102.0+1.21")
}
sourceSets {
    main {
        java {
            srcDir("java")
        }
    }
}