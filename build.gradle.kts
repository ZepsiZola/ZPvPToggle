plugins {
    kotlin("jvm") version "1.9.22"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "zepsizola.me"
version = "1.4"

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
    
    // bStats - this will be included in the JAR
    implementation("org.bstats:bstats-bukkit:3.0.2")
    
    // HikariCP for database connection pooling - will be included in the JAR
    implementation("com.zaxxer:HikariCP:4.0.3")
    
    // Database dependencies - using compileOnly to avoid bundling them
    compileOnly("org.xerial:sqlite-jdbc:3.43.0.0")
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
        relocate("org.bstats", "zepsizola.me.zPvPToggle.bstats")
        relocate("com.zaxxer.hikari", "zepsizola.me.zPvPToggle.libs.hikari")
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
