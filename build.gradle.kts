plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {}

group = "fr.codinbox.connector"
version = "6.0.2"

val targetJavaVersion = JavaVersion.VERSION_17
java {
    sourceCompatibility = targetJavaVersion
    targetCompatibility = targetJavaVersion
    if (JavaVersion.current() < targetJavaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.majorVersion))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    if (targetJavaVersion >= JavaVersion.VERSION_1_10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion.majorVersion.toInt()) // The string represent a number, like "1" for Java1
    }

    options.encoding = Charsets.UTF_8.name()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
