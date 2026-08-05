package com.pucetec.users

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Prueba de integracion HTTP real (MockMvc, sin mockear el filtro de
 * seguridad): un endpoint protegido sin token debe devolver 401. Este micro
 * no tiene roles diferenciados (todo usuario autenticado tiene los mismos
 * permisos), asi que no aplica un caso de 403 por rol equivocado aqui.
 */
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:userssecuritydb;DB_CLOSE_DELAY=-1",
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
    fun `listing users without a token returns 401`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `listing users with a valid token returns 200`() {
        mockMvc.perform(get("/api/users").with(jwt()))
            .andExpect(status().isOk)
    }
}
