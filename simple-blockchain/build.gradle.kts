import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    java
}

group = "io.collective.blockchain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
        exceptionFormat = FULL
    }
}
