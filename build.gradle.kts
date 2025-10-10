import org.codehaus.groovy.runtime.DefaultGroovyMethods.mixin

plugins {
    id("java")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("dev.architectury.loom") version("1.7-SNAPSHOT")

    kotlin("jvm") version "1.9.23"
}

group = "dev.elrol.arrow.wormholes"
version = "1.0.0"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven(url = "https://maven.nucleoid.xyz")
    maven(url = "https://maven.tomalbrc.de")
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven(url = "https://maven.impactdev.net/repository/development/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://repo.phoenix616.dev")
    maven(url = "https://maven.enginehub.org/repo/")
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroup("software.bernie.geckolib")
        }
    }
}

dependencies {
    minecraft ("com.mojang:minecraft:1.21.1")
    mappings ("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.104.0+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.104.0+1.21.1"))

    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
    modImplementation("com.cobblemon:fabric:1.6.0+1.21.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    implementation("com.mysql", "mysql-connector-j","9.2.0")

    modImplementation("com.sk89q.worldedit:worldedit-fabric-mc1.21:7.3.8")
    modImplementation("software.bernie.geckolib:geckolib-fabric-${project.property("minecraft_version")}:${project.property("geckolib_version")}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}

fabricApi {
    configureDataGeneration()
}