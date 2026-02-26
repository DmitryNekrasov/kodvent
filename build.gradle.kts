import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.3.10"
    kotlin("plugin.power-assert") version "2.3.10"
    id("org.jetbrains.dokka") version "2.2.0-Beta"
    id("com.gradleup.nmcp") version "1.4.4"
    id("com.gradleup.nmcp.aggregation") version "1.4.4"
    `maven-publish`
    signing
}

group = "io.github.dmitrynekrasov"
version = "0.3.1"

repositories {
    mavenCentral()
}

dependencies {
    nmcpAggregation(project(":"))
}

kotlin {
    explicitApi()

    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xreturn-value-checker=full")
    }

    jvmToolchain(25)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    js(IR) {
        nodejs()
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Native - Linux
    linuxX64()
    linuxArm64()

    // Native - macOS
    macosX64()
    macosArm64()

    // Native - Windows
    mingwX64()

    // Native - iOS
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    // Native - watchOS
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()

    // Native - tvOS
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf(
        "kotlin.check",
        "kotlin.test.assertTrue",
        "kotlin.test.assertFalse",
        "kotlin.test.assertEquals",
        "kotlin.test.assertNotEquals",
        "kotlin.test.assertNull",
        "kotlin.test.assertNotNull"
    )
}

dokka {
    dokkaPublications.html {
        failOnWarning.set(true)
        outputDirectory.set(rootDir.resolve("docs"))
    }

    dokkaSourceSets.named("commonMain") {
        samples.from("src/commonTest/kotlin/samples")
        sourceLink {
            remoteUrl("https://github.com/DmitryNekrasov/kodvent/tree/v$version")
        }
    }
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(dokkaJavadocJar)

        pom {
            name.set("kodvent")
            description.set("A Kotlin utility library for Advent of Code challenges")
            url.set("https://github.com/DmitryNekrasov/kodvent")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("DmitryNekrasov")
                    name.set("Dmitry Nekrasov")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/DmitryNekrasov/kodvent.git")
                developerConnection.set("scm:git:ssh://github.com/DmitryNekrasov/kodvent.git")
                url.set("https://github.com/DmitryNekrasov/kodvent")
            }
        }
    }
}

signing {
    val signingKeyFile = project.findProperty("signing.keyFile") as String?
    val signingPassword = project.findProperty("signing.password") as String?

    if (signingKeyFile != null && signingPassword != null) {
        val keyFile = file(signingKeyFile)
        if (keyFile.exists()) {
            val signingKey = keyFile.readText()
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }

    sign(publishing.publications)
}

// Ensure all signing tasks complete before any publish task starts,
// because all publications share the same KDoc jar artifact and its .asc signature.
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

nmcpAggregation {
    centralPortal {
        username = project.findProperty("sonatype.username") as String? ?: System.getenv("SONATYPE_USERNAME")
        password = project.findProperty("sonatype.password") as String? ?: System.getenv("SONATYPE_PASSWORD")
        publishingType = "USER_MANAGED"
    }
}
