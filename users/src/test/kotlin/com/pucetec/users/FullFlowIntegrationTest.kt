package com.pucetec.users

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * Recorre el flujo completo de perfiles via HTTP real (MockMvc + Spring
 * Security real, JwtDecoder mockeado): crear -> consultar propio ->
 * actualizar -> listar -> consultar por id/cognitoId -> eliminar, mas los
 * casos de error (nombre en blanco, cognitoId duplicado, no encontrado).
 */
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:usersflowdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    ]
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FullFlowIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    companion object {
        private var userId: Long = 0
    }

    private fun authAs(cognitoId: String) = jwt().jwt { it.subject(cognitoId) }

    @Test
    @Order(1)
    fun `01 - creates a profile for the authenticated user`() {
        val body = mockMvc.perform(
            post("/api/users/me").with(authAs("cognito-flow-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Ana Flow","email":"ana@puce.edu.ec","phone":"0999999999"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cognitoId").value("cognito-flow-1"))
            .andReturn().response.contentAsString
        userId = objectMapper.readTree(body)["id"].asLong()
    }

    @Test
    @Order(2)
    fun `02 - creating a second profile for the same cognitoId returns 409`() {
        mockMvc.perform(
            post("/api/users/me").with(authAs("cognito-flow-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Duplicate","email":null,"phone":null}""")
        ).andExpect(status().isConflict)
    }

    @Test
    @Order(3)
    fun `03 - creating a profile with a blank name returns 400`() {
        mockMvc.perform(
            post("/api/users/me").with(authAs("cognito-flow-2"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","email":null,"phone":null}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    @Order(4)
    fun `04 - gets the authenticated user's own profile`() {
        mockMvc.perform(get("/api/users/me").with(authAs("cognito-flow-1")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Ana Flow"))
    }

    @Test
    @Order(5)
    fun `05 - updates the authenticated user's own profile`() {
        mockMvc.perform(
            put("/api/users/me").with(authAs("cognito-flow-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Ana Updated","email":"ana@puce.edu.ec","phone":"0999999999"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Ana Updated"))
    }

    @Test
    @Order(6)
    fun `06 - lists every user`() {
        mockMvc.perform(get("/api/users").with(authAs("any")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").exists())
    }

    @Test
    @Order(7)
    fun `07 - gets a user by id`() {
        mockMvc.perform(get("/api/users/$userId").with(authAs("any")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Ana Updated"))
    }

    @Test
    @Order(8)
    fun `08 - getting a nonexistent user by id returns 404`() {
        mockMvc.perform(get("/api/users/999999").with(authAs("any")))
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(9)
    fun `09 - gets a user by cognitoId`() {
        mockMvc.perform(get("/api/users/cognito/cognito-flow-1").with(authAs("any")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
    }

    @Test
    @Order(10)
    fun `10 - deletes the user`() {
        mockMvc.perform(delete("/api/users/$userId").with(authAs("any")))
            .andExpect(status().isNoContent)
    }

    @Test
    @Order(11)
    fun `11 - deleting an already-deleted user returns 404`() {
        mockMvc.perform(delete("/api/users/$userId").with(authAs("any")))
            .andExpect(status().isNotFound)
    }
}
