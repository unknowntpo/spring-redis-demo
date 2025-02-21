/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.10.2/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.logging)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.postgresql)
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.boot.starter.test)

    // mapper
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    // Add Lombok MapStruct binding
    annotationProcessor(libs.lombok.mapstruct.binding)

    // lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // JMH dependencies
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.App"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform {
        project.findProperty("excludeTags")?.toString()?.split(",")?.let { tags ->
            excludeTags(*tags.toTypedArray())
        }
		// Handle include tags
        project.findProperty("includeTags")?.toString()?.split(",")?.let { tags ->
            includeTags(*tags.toTypedArray())
        }
    }
}

tasks.register<JavaExec>("runBenchmark") {
    description = "Runs JMH benchmark"
    mainClass.set("org.example.benchmarks.StudentEndpointBenchmark")
    classpath = sourceSets["test"].runtimeClasspath
    
    // Default JVM args
    jvmArgs = listOf(
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+DebugNonSafepoints",
        "-Xrunjdwp:none"
    )
    
    // Pass through system properties
    systemProperties(System.getProperties().map { 
        it.key.toString() to it.value.toString() 
    }.toMap())
}
