plugins {
    id("java")
    id("application")
    kotlin("jvm")
}

group = "com.amarildo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "com.amarildo.Main"
}

dependencies {
    implementation("commons-cli:commons-cli:1.9.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains:annotations:26.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
        attributes["Main-Class"] = "com.amarildo.Main"
    }
}
kotlin {
    jvmToolchain(21)
}
