plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("java")
    id ("application")
    id ("org.openjfx.javafxplugin") version "0.0.10"
    id ("org.beryx.jlink") version "2.24.1"

    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
    kotlin("plugin.serialization").version("1.7.10")
}

jlink {
    launcher {
        name = "console"
    }
    imageZip.set(project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip"))
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
}

tasks.jlinkZip {
    group = "distribution"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    // Define the main class for the application.
    mainClassName = "todo.console.MainKt"
}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-client-core:2.1.2")
    implementation("io.ktor:ktor-client-cio:2.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.2")
    implementation(project(":dtos"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
