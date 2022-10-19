import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id ("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
	id("org.springframework.boot") version "3.0.0-M5"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	id("java")
	id ("application")
	id ("org.openjfx.javafxplugin") version "0.0.10"
	id ("org.beryx.jlink") version "2.24.1"

	kotlin("jvm") version "1.7.10"
	kotlin("plugin.spring") version "1.7.10"
	kotlin("plugin.jpa") version "1.7.10"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

javafx {
	version = "16"
	modules("javafx.controls", "javafx.fxml")
}


dependencies {
	implementation ("org.xerial:sqlite-jdbc:3.39.3.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
//	implementation("org.jetbrains.exposed", "exposed-core", "0.40.1")
//	implementation("org.jetbrains.exposed", "exposed-dao", "0.40.1")
//	implementation("org.jetbrains.exposed", "exposed-jdbc", "0.40.1")
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
