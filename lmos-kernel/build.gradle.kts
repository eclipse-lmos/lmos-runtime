dependencies {
    api("org.slf4j:slf4j-api:2.0.17")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.10.2")
    api("io.micrometer:micrometer-core:1.15.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation("org.assertj:assertj-core:3.27.4")
    //testImplementation("org.mockito:mockito-core:5.19.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
}