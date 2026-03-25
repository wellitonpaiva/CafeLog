plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.welliton"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.jackson.datatype)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.kotlinx.html)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

kotlin {
    jvmToolchain(23)
}

tasks.test {
    useJUnitPlatform()
}