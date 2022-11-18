plugins {
    id("java")
    id ("application")
    id ("org.openjfx.javafxplugin") version "0.0.10"
    id ("org.beryx.jlink") version "2.24.1"

    `java-library`

    kotlin("plugin.serialization").version("1.7.20")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

jlink {
    launcher {
        name = "TrackIT"
    }
    imageZip.set(project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip"))
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
}

tasks.jlinkZip {
    group = "distribution"
}

repositories {
    mavenCentral()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    // Define the main class for the application.
    mainClassName = "todo.ui.MainKt"
}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    api(project(":dtos"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-client-core:2.1.2")
    implementation("io.ktor:ktor-client-cio:2.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.2")
    implementation("org.controlsfx:controlsfx:11.1.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
