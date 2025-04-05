plugins {
    java
    id("io.quarkus")
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
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
    implementation(kotlin("stdlib"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-rest-client-jackson")
    implementation("org.apache.commons:commons-compress:1.27.0")
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
//    implementation("org.eclipse.lmos:lmos-starter:0.0.1-SNAPSHOT")
    implementation("org.eclipse.lmos:arc-agent-client:0.1.0-SNAPSHOT")
    implementation("org.eclipse.lmos:arc-api:0.1.0-SNAPSHOT")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:slf4j-api:2.0.9")

    implementation(files("libs/lmos-starter-0.0.1-SNAPSHOT.jar"))


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
    jvmToolchain(23)
}