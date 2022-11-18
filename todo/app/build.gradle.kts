import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id ("java")
    id ("application")
    id ("org.springframework.boot") version "2.7.4"
    id ("io.spring.dependency-management") version "1.0.14.RELEASE"
    id ("org.openjfx.javafxplugin") version "0.0.10"
    id ("org.beryx.jlink") version "2.24.1"

    `java-library`

    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
}

group = "com.example"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("todo.app.AppKt")
}

dependencies {
    api(project(":dtos"))

    implementation ("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.controlsfx:controlsfx:11.1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
