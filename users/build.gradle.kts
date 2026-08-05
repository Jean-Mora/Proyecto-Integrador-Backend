plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
	jacoco
}

group = "com.pucetec"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// Seguridad: valida el JWT emitido por Cognito (resource server)
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	// Solo para que el @SpringBootTest de contexto no necesite una Postgres real
	// corriendo; la app en runtimeOnly usa exclusivamente Postgres.
	testRuntimeOnly("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

// Desactiva el jar plano (sin dependencias): solo se necesita el bootJar
// ejecutable para el Dockerfile, y asi build/libs/*.jar no queda ambiguo.
tasks.named<Jar>("jar") {
	enabled = false
}

jacoco {
	toolVersion = "0.8.12"
}

val coverageExclusions = listOf(
	"com/pucetec/users/UsersApplication*",
	"com/pucetec/users/config/**",
	"com/pucetec/users/dto/**",
	"com/pucetec/users/entities/**"
)

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
		html.required = true
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) { exclude(coverageExclusions) }
		})
	)
}
