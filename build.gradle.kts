plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.sudeshsingh.matcher"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // --- HTTP API layer ---
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")

    // --- Embedding cache (swap TF-IDF for a real embedding call cached here) ---
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

    // --- Vector similarity at scale (pgvector extension over Postgres) ---
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("matcher.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
