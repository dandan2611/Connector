plugins {
    id("java")
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "fr.codinbox.connector"
version = "6.0.2"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.1.0")

    // Redis
    api("org.redisson:redisson:3.29.0")

    // MySql
    api("com.zaxxer:HikariCP:5.1.0")

    // Json
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven("https://nexus.codinbox.fr/repository/maven-releases/") {
            name = "public-releases"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
