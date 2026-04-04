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
version = "7.0.1"

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

val javadocClasspath: Configuration by configurations.creating {
    isTransitive = true
}

dependencies {
    javadocClasspath("org.redisson:redisson:3.29.0")
    javadocClasspath("com.zaxxer:HikariCP:5.1.0")
    javadocClasspath("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    javadocClasspath("com.rabbitmq:amqp-client:5.21.0")
    javadocClasspath("org.apache.kafka:kafka-clients:3.7.0")
    javadocClasspath("org.jetbrains:annotations:24.1.0")
}

data class LibSource(val artifact: String, val includes: List<String>, val excludes: List<String> = emptyList())

val librarySpecs = listOf(
    LibSource("org.redisson:redisson:3.29.0:sources",
        includes = listOf("org/redisson/api/**", "org/redisson/client/**", "org/redisson/config/**", "org/redisson/codec/JsonJacksonCodec.java"),
        excludes = listOf("**/WorkerOptions.java", "**/RedisClient.java", "**/RedissonNodeConfig.java")
    ),
    LibSource("com.zaxxer:HikariCP:5.1.0:sources",
        includes = listOf("com/zaxxer/hikari/HikariDataSource.java")
    ),
    LibSource("org.mariadb.jdbc:mariadb-java-client:3.4.0:sources",
        includes = listOf("org/mariadb/jdbc/Driver.java", "org/mariadb/jdbc/MariaDbDataSource.java")
    ),
    LibSource("com.rabbitmq:amqp-client:5.21.0:sources",
        includes = listOf("com/rabbitmq/client/**"),
        excludes = listOf("**/impl/MicrometerMetricsCollector.java", "**/impl/OpenTelemetryMetricsCollector.java",
            "**/impl/StandardMetricsCollector.java", "**/observation/**")
    ),
    LibSource("org.apache.kafka:kafka-clients:3.7.0:sources",
        includes = listOf("org/apache/kafka/clients/producer/**", "org/apache/kafka/clients/consumer/**",
            "org/apache/kafka/clients/admin/**", "org/apache/kafka/common/serialization/**")
    ),
    LibSource("org.jetbrains:annotations:24.1.0:sources",
        includes = listOf("org/jetbrains/annotations/**")
    )
)

val unpackJavadocSources by tasks.registering {
    val outputDir = layout.buildDirectory.dir("javadoc-sources")
    outputs.dir(outputDir)

    doLast {
        librarySpecs.forEach { spec ->
            val resolved = configurations.detachedConfiguration(
                dependencies.create(spec.artifact)
            ).resolve().first()
            copy {
                from(zipTree(resolved))
                into(outputDir)
                include(spec.includes.map { if (it.endsWith(".java")) it else "$it/*.java" })
                exclude("**/module-info.java")
                exclude(spec.excludes)
            }
        }
    }
}

tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc including library sources."

    val commonsProject = project(":commons")
    val velocityProject = project(":velocity")

    dependsOn(commonsProject.tasks.named("classes"), velocityProject.tasks.named("classes"), unpackJavadocSources)

    source(commonsProject.sourceSets["main"].allJava)
    source(velocityProject.sourceSets["main"].allJava.matching {
        include("**/Connector.java")
    })
    source(layout.buildDirectory.dir("javadoc-sources"))

    classpath = files(
        commonsProject.sourceSets["main"].compileClasspath,
        velocityProject.sourceSets["main"].compileClasspath,
        javadocClasspath
    )

    destinationDir = file("${layout.buildDirectory.get()}/docs/javadoc")

    options {
        this as StandardJavadocDocletOptions
        encoding = "UTF-8"
        charSet = "UTF-8"
        addBooleanOption("Xdoclint:none", true)
        links("https://docs.oracle.com/en/java/javase/21/docs/api/")
    }
}
