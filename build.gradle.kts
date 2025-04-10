plugins {
    kotlin("jvm") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "zepsizola.me"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // PaperMC repository for Paper API and Folia
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    // Sonatype repository for additional dependencies if needed
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    // Compile-only dependency for Paper API (Folia-compatible)
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Implementation dependency for Kotlin standard library
    implementation(kotlin("stdlib"))
}

val targetJavaVersion = 17 // Set to 17 or 21 based on your server's Java version

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}