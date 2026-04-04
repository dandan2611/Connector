plugins {
    id("java")
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.0"
}

group = "fr.codinbox.connector"
version = "7.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.1.0")

    // Redis
    api("org.redisson:redisson:3.29.0")

    // Database
    api("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.0")

    // RabbitMQ
    api("com.rabbitmq:amqp-client:5.21.0")

    // Kafka
    api("org.apache.kafka:kafka-clients:3.7.0")

    // Json
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.0")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.6")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:none", true)
        encoding = "UTF-8"
        charSet = "UTF-8"
    }
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
