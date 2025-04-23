plugins {
    kotlin("jvm") version "1.9.22"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "zepsizola.me"
version = "1.2"

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
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib"))
    implementation("org.bstats:bstats-bukkit:3.0.2")
    
    // Database dependencies
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("mysql:mysql-connector-java:8.0.33")
}

val targetJavaVersion = 21 // Set to 17 based on Gradle compatibility

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        // Fix the relocation path to match your actual package structure
        relocate("org.bstats", "zepsizola.me.zPvPToggle.bstats")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        minimize()
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
