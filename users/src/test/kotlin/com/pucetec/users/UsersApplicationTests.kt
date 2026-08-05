package com.pucetec.users

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean

// Esta prueba de contexto usa H2 en memoria SOLO para el classpath de test
// (ver testRuntimeOnly en build.gradle.kts); la app real (runtimeOnly) usa
// Postgres. Asi `./gradlew test` no necesita una Postgres corriendo, sin que
// H2 termine formando parte del jar de produccion.
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:usersdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    ]
)
class UsersApplicationTests {

    // Reemplazamos el JwtDecoder real por un mock. Asi el contexto levanta
    // sin salir a la red a descargar las llaves publicas de Cognito.
    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun contextLoads() {
    }
}
