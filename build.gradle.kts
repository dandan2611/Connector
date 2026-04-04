plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
}

dependencies {}

group = "fr.codinbox.connector"
version = "7.0.0"

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

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for commons module and Velocity Connector accessor."

    val commonsProject = project(":commons")
    val velocityProject = project(":velocity")

    dependsOn(commonsProject.tasks.named("classes"), velocityProject.tasks.named("classes"))

    source(commonsProject.sourceSets["main"].allJava)
    source(velocityProject.sourceSets["main"].allJava.matching {
        include("**/Connector.java")
    })

    classpath = files(
        commonsProject.sourceSets["main"].compileClasspath,
        velocityProject.sourceSets["main"].compileClasspath
    )

    destinationDir = file("${layout.buildDirectory.get()}/docs/javadoc")

    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        charSet = "UTF-8"
        addBooleanOption("Xdoclint:none", true)
        links(
            "https://docs.oracle.com/en/java/javase/21/docs/api/",
            "https://javadoc.io/static/org.redisson/redisson/3.29.0/",
            "https://javadoc.io/static/com.zaxxer/HikariCP/5.1.0/",
            "https://javadoc.io/static/org.mariadb.jdbc/mariadb-java-client/3.4.0/",
            "https://javadoc.io/static/com.rabbitmq/amqp-client/5.21.0/",
            "https://javadoc.io/static/org.apache.kafka/kafka-clients/3.7.0/",
            "https://javadoc.io/static/org.jetbrains/annotations/24.1.0/"
        )
    }
}
