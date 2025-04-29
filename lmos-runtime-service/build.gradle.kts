import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.citi.helm")
    id("com.citi.helm-publish")
}

dependencies {

    val springBootVersion: String by rootProject.extra

    implementation(project(":lmos-runtime-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    testImplementation(testFixtures(project(":lmos-runtime-core")))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
}

// Set kotlinx-serialization version in dependencyManagement to overrule the dependency management of spring boot plugin.
// Can be omitted again when spring boot has upgraded to more recent kotlinx-serialization version.
dependencyManagement {
    dependencies {
        val kotlinxSerializationVersion = "1.8.1"
        dependency("org.jetbrains.kotlinx:kotlinx-serialization-bom:$kotlinxSerializationVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
    }
}

tasks.named<BootBuildImage>("bootBuildImage") {
    group = "publishing"
    if ((System.getenv("REGISTRY_URL") ?: project.findProperty("REGISTRY_URL")) != null) {
        val registryUrl = getProperty("REGISTRY_URL")
        val registryUsername = getProperty("REGISTRY_USERNAME")
        val registryPassword = getProperty("REGISTRY_PASSWORD")
        val registryNamespace = getProperty("REGISTRY_NAMESPACE")

        imageName.set("$registryUrl/$registryNamespace/${rootProject.name}:${project.version}")
        publish = true
        docker {
            publishRegistry {
                url.set(registryUrl)
                username.set(registryUsername)
                password.set(registryPassword)
            }
        }
    } else {
        imageName.set("${rootProject.name}:${project.version}")
        publish = false
    }
}

helm {
    charts {
        create("main") {
            chartName.set("${rootProject.name}-chart")
            chartVersion.set("${project.version}")
            sourceDir.set(file("src/main/helm"))
        }
    }
}

tasks.register("replaceChartVersion") {
    doLast {
        val chartFile = file("src/main/helm/Chart.yaml")
        val content = chartFile.readText()
        val updatedContent = content.replace("\${chartVersion}", "${project.version}")
        chartFile.writeText(updatedContent)
    }
}

tasks.register("helmPush") {
    description = "Push Helm chart to OCI registry"
    group = "publishing"
    dependsOn(tasks.named("helmPackageMainChart"))

    doLast {
        group = "publishing"
        if ((System.getenv("REGISTRY_URL") ?: project.findProperty("REGISTRY_URL")) != null) {
            val registryUrl = getProperty("REGISTRY_URL")
            val registryUsername = getProperty("REGISTRY_USERNAME")
            val registryPassword = getProperty("REGISTRY_PASSWORD")
            val registryNamespace = getProperty("REGISTRY_NAMESPACE")

            helm.execHelm("registry", "login") {
                option("-u", registryUsername)
                option("-p", registryPassword)
                args(registryUrl)
            }

            helm.execHelm("push") {
                args(
                    tasks
                        .named("helmPackageMainChart")
                        .get()
                        .outputs.files.singleFile
                        .toString(),
                )
                args("oci://$registryUrl/$registryNamespace")
            }

            helm.execHelm("registry", "logout") {
                args(registryUrl)
            }
        }
    }
}

tasks.named("publish") {
    dependsOn(tasks.named<BootBuildImage>("bootBuildImage"))
    dependsOn(tasks.named("helmPush"))
}

fun getProperty(propertyName: String) = System.getenv(propertyName) ?: project.findProperty(propertyName) as String
