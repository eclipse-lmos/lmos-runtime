plugins {
    java
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.apache.commons:commons-compress:1.27.0")
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("org.eclipse.lmos:lmos-starter:0.0.1-SNAPSHOT")
    implementation("org.eclipse.lmos:arc-agent-client:0.1.0-SNAPSHOT")
    implementation("org.eclipse.lmos:arc-api:0.1.0-SNAPSHOT")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")

    testImplementation("io.quarkus:quarkus-junit5")
}

group = "org.acme"
version = "1.0.0-SNAPSHOT"

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

kotlin {
    jvmToolchain(21)
}