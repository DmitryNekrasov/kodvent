plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "me.scannorone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
