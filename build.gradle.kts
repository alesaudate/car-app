import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
	id("org.springframework.boot") version "2.4.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
	kotlin("plugin.noarg") version "1.4.32"
	kotlin("plugin.allopen") version "1.4.32"
}

apply(from = "gradle/integrationTest.gradle")
apply(from = "gradle/coverage.gradle")
apply(from = "gradle/lint.gradle")

group = "com.github.alesaudate.samples.reactive"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11



repositories {
	mavenCentral()
}

val testContainersVersion="1.15.3"
val liquibaseVersion="4.3.5"
val springdocVersion="1.5.8"

dependencies {

	implementation("com.jayway.jsonpath:json-path")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-noarg")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.liquibase:liquibase-core:$liquibaseVersion") // The managed version has issues with dates: https://stackoverflow.com/questions/66787654/class-java-time-localdatetime-cannot-be-cast-to-class-java-lang-string
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.hateoas:spring-hateoas")
	implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdocVersion")

	runtimeOnly("mysql:mysql-connector-java")


	testImplementation("com.github.javafaker:javafaker:1.0.2")
	testImplementation("com.ninja-squad:springmockk:3.0.1")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit-vintage-engine")
		exclude(module = "mockito-core")
	}
	testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
	testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

allOpen {
	annotation("javax.persistence.Entity")
}

noArg {
	annotation("javax.persistence.Entity")
}