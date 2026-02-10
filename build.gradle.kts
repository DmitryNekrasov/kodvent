plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("com.gradleup.nmcp") version "1.4.4"
    id("com.gradleup.nmcp.aggregation") version "1.4.4"
    `maven-publish`
    signing
}

group = "io.github.dmitrynekrasov"
version = "0.2.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    nmcpAggregation(project(":"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xreturn-value-checker=full")
        freeCompilerArgs.add("-jvm-target=1.8")
    }
}

dokka {
    dokkaSourceSets.configureEach {
        samples.from("src/test/kotlin/samples")
    }
}

java {
    withSourcesJar()
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(dokkaJavadocJar)

            pom {
                name.set("kodvent")
                description.set("A Kotlin utility library for Advent of Code challenges")
                url.set("https://github.com/DmitryNekrasov/kodvent")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

    sign(publishing.publications["maven"])
}

nmcpAggregation {
    centralPortal {
        username = project.findProperty("sonatype.username") as String? ?: System.getenv("SONATYPE_USERNAME")
        password = project.findProperty("sonatype.password") as String? ?: System.getenv("SONATYPE_PASSWORD")
        publishingType = "USER_MANAGED"
    }
}
