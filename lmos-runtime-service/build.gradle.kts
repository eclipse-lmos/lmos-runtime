import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.github.build-extensions-oss.helm")
    id("io.github.build-extensions-oss.helm-publish")
}

dependencies {
    implementation(platform(project(":lmos-runtime-bom")))
    implementation(project(":lmos-runtime-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation(testFixtures(project(":lmos-runtime-core")))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.redis.testcontainers:testcontainers-redis")
    testImplementation("org.springframework.boot:spring-boot-webtestclient")
    testImplementation("org.springframework.boot:spring-boot-webflux-test")
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
        create("${rootProject.name}-chart") {
            sourceDir.set(file("src/main/helm"))
        }
    }
}

tasks.register("helmPush") {
    description = "Push Helm chart to OCI registry"
    group = "publishing"
    dependsOn(tasks.named("helmPackageLmosRuntimeChartChart"))

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
                        .named("helmPackageLmosRuntimeChartChart")
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
