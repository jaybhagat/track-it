plugins {
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
    id ("java")
    id ("application")
}

group = "com.example"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

application {
    // Define the main class for the application.
    mainClass.set("todo.dtos.AppKt")
}

dependencies {
    implementation ("io.ktor:ktor-serialization-kotlinx-json-jvm:2.1.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
