import info.solidsoft.gradle.pitest.PitestTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
	id("info.solidsoft.pitest") version "1.5.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("name.remal.sonarlint") version "1.3.1"
	id("org.owasp.dependencycheck") version "6.1.6"
	id("org.springframework.boot") version "2.4.5"
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
	kotlin("plugin.noarg") version "1.4.32"
	kotlin("plugin.allopen") version "1.4.32"
}

apply(from = "gradle/integrationTest.gradle")
apply(from = "gradle/coverage.gradle")
apply(from = "gradle/lint.gradle")
apply(from = "gradle/sonar.gradle")

group = "com.github.alesaudate.samples.reactive"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11



repositories {
	mavenCentral()
}

val testContainersVersion="1.15.3"
val liquibaseVersion="4.3.5"
val springdocVersion="1.5.8"
val resilience4jVersion="1.7.0"

dependencies {

	implementation("com.jayway.jsonpath:json-path")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")
	implementation("io.github.resilience4j:resilience4j-spring-boot2:$resilience4jVersion")
	implementation("io.micrometer:micrometer-core")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.apache.commons:commons-text:1.9")
	implementation("org.jetbrains.kotlin:kotlin-noarg")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.liquibase:liquibase-core:$liquibaseVersion") // The managed version has issues with dates: https://stackoverflow.com/questions/66787654/class-java-time-localdatetime-cannot-be-cast-to-class-java-lang-string
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-log4j2")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.hateoas:spring-hateoas")
	implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdocVersion")

	runtimeOnly("mysql:mysql-connector-java")

	testImplementation("com.github.javafaker:javafaker:1.0.2")
	testImplementation("com.ninja-squad:springmockk:3.0.1")
	testImplementation("com.tngtech.archunit:archunit:0.19.0")
	testImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.4.0")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("io.rest-assured:spring-mock-mvc:4.3.2")
	testImplementation("org.pitest:pitest-junit5-plugin:0.12")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit-vintage-engine")
		exclude(module = "mockito-core")
		exclude(module = "spring-boot-starter-logging")
	}
	testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:2.2.5.RELEASE")
	testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
	testImplementation("org.testcontainers:testcontainers:$testContainersVersion")

}

configurations {
	all {
		exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
	}
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

tasks.withType<PitestTask> {
	testPlugin.set("junit5")
	excludedClasses.set(setOf("*.config.*"))
	mutationThreshold.set(85)
	excludedTestClasses.set(setOf("**IT"))
	threads.set(8)
	timestampedReports.set(false)
	outputFormats.set(setOf("HTML"))
	reportDir.set(file("$buildDir/reports/pitest"))
	mutators.set(setOf("STRONGER", "DEFAULTS"))
	avoidCallsTo.set(setOf("kotlin.jvm.internal", "kotlinx.coroutines", "org.slf4j"))
	historyInputLocation.set(file("$projectDir/pitest/pitest.history"))
	historyOutputLocation.set(file("$projectDir/pitest/pitest.history"))
}

dependencyCheck {
	analyzers(closureOf<org.owasp.dependencycheck.gradle.extension.AnalyzerExtension> {
		nodeEnabled = false
		assemblyEnabled = false
		nodeAuditEnabled = false
		nodeAudit(closureOf<org.owasp.dependencycheck.gradle.extension.NodeAuditExtension> {
			yarnEnabled = false
		})
	})
}

allOpen {
	annotation("javax.persistence.Entity")
}

noArg {
	annotation("javax.persistence.Entity")
}

tasks.bootRun {
	if (project.hasProperty("args")) {
		val projectArgs = project.properties["args"] as String
		args(projectArgs.split(','))
	}
}

tasks.check {
	dependsOn("pitest")
	dependsOn("dependencyCheckAnalyze")
}

