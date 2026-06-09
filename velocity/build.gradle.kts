plugins {
    java
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.0"
}

group = "fr.codinbox.connector"
version = "8.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":commons"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

val targetJavaVersion = JavaVersion.VERSION_21
java {
    sourceCompatibility = targetJavaVersion
    targetCompatibility = targetJavaVersion
    if (JavaVersion.current() < targetJavaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.majorVersion))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    if (targetJavaVersion >= JavaVersion.VERSION_1_10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion.majorVersion.toInt())
    }

    options.encoding = Charsets.UTF_8.name()
}

tasks.shadowJar {
    archiveBaseName.set("connector-velocity")
    relocate("org.redisson", "fr.codinbox.connector.libs.redisson")
    relocate("com.zaxxer.hikari", "fr.codinbox.connector.libs.hikari")
    relocate("com.rabbitmq", "fr.codinbox.connector.libs.rabbitmq")
    relocate("com.fasterxml.jackson", "fr.codinbox.connector.libs.jackson")
}

tasks.jar {
    archiveBaseName.set("connector-velocity")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources.configure {
    val props = mapOf(Pair("version", version))

    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
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
