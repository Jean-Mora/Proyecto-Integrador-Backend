package com.puce.sigpel

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Prueba de integracion end-to-end (HTTP real via MockMvc, sin mockear el
 * SecurityFilterChain) para verificar el criterio 8 de la rubrica:
 * sin token -> 401, token con rol equivocado -> 403, token con el rol
 * correcto -> pasa la autorizacion.
 *
 * El JwtDecoder se reemplaza por un mock para no salir a la red a validar
 * contra el JWKS real de Cognito (igual que en users/UsersApplicationTests).
 * La base de datos es H2 en memoria solo para esta prueba (ver build.gradle.kts,
 * testRuntimeOnly); la app real usa exclusivamente Postgres.
 */
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:sigpeldb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    ]
)
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `creating a category without a token returns 401`() {
        mockMvc.perform(
            post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"No auth category"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `creating a category with the ESTUDIANTE role returns 403`() {
        mockMvc.perform(
            post("/categories")
                .with(jwt().authorities(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ESTUDIANTE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Wrong role category"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `creating a category with the ENCARGADO role succeeds`() {
        mockMvc.perform(
            post("/categories")
                .with(jwt().authorities(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ENCARGADO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Integration test category"}""")
        ).andExpect(status().isCreated)
    }

    @Test
    fun `requesting a loan without a token returns 401`() {
        mockMvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"equipmentId":1}""")
        ).andExpect(status().isUnauthorized)
    }
}
