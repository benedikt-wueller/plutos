import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposedVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

group = "dev.benedikt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.40.0.0")

    // Webserver
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // CSV Importer
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10")

    // XML
    implementation("com.sun.xml.bind:jaxb-ri:3.0.2") {
        exclude("com.sun.xml.bind", "jaxb-release-documentation")
        exclude("com.sun.xml.bind", "jaxb-samples")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.benedikt.plutos.PlutosKt"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.shadowJar {
    this.destinationDirectory.set(layout.buildDirectory.dir("plutos"))
    this.archiveBaseName.set("plutos")
    this.archiveClassifier.set("")
    this.archiveVersion.set("")
}

tasks {
    val buildClient = register<Exec>("buildClient") {
        // TODO: make this task platform agnostic
        this.workingDir("client")
        commandLine("cmd", "/c", "npm run build")
    }
    val copyClient = register<Copy>("copyClient") {
        from("client/dist")
        into(layout.buildDirectory.dir("plutos/client/dist"))
    }
    register("plutos") {
        delete("${project.buildDir}/plutos")
        dependsOn(buildClient)
        dependsOn(copyClient)
        dependsOn(shadowJar)
    }
}
