package com.puce.sigpel

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Casos que el flujo principal (FullFlowIntegrationTest) no ejercita:
 * 404, 400 (validacion y fecha invalida), 409 por nombre duplicado,
 * actualizar estado de equipo, rechazar un prestamo, cancelar un
 * prestamo (propio y ajeno).
 */
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:sigpeledgedb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    ]
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class EdgeCasesIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    companion object {
        private var categoryId: Long = 0
        private var equipmentId: Long = 0
    }

    private fun staff() = jwt().jwt { it.subject("staff-2") }.authorities(SimpleGrantedAuthority("ROLE_ENCARGADO"))
    private fun student(name: String) = jwt().jwt { it.subject(name) }.authorities(SimpleGrantedAuthority("ROLE_ESTUDIANTE"))

    @Test
    @Order(1)
    fun `getting a nonexistent equipment returns 404`() {
        mockMvc.perform(get("/equipment/999999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @Order(2)
    fun `creating a category with a blank name returns 400`() {
        mockMvc.perform(
            post("/categories").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":""}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    @Order(3)
    fun `creating the same category twice returns 409 the second time`() {
        val body = """{"name":"Edge Case Category"}"""
        mockMvc.perform(post("/categories").with(staff()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated)

        mockMvc.perform(post("/categories").with(staff()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict)
    }

    @Test
    @Order(4)
    fun `setup - create category and equipment for the rest of this suite`() {
        val categoryBody = mockMvc.perform(
            post("/categories").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Edge Case Equipment Category"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        categoryId = objectMapper.readTree(categoryBody)["id"].asLong()

        val equipmentBody = mockMvc.perform(
            post("/equipment").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"categoryId":$categoryId,"name":"Edge Case Equipment","serialNumber":"EDGE-CASE-001"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        equipmentId = objectMapper.readTree(equipmentBody)["id"].asLong()
    }

    @Test
    @Order(5)
    fun `staff updates equipment status directly to MAINTENANCE`() {
        mockMvc.perform(
            patch("/equipment/$equipmentId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"MAINTENANCE"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("MAINTENANCE"))

        // Back to AVAILABLE so the rest of the suite can request it as a loan.
        mockMvc.perform(
            patch("/equipment/$equipmentId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"AVAILABLE"}""")
        ).andExpect(status().isOk)
    }

    @Test
    @Order(6)
    fun `requesting a loan with a past estimated return date returns 400`() {
        val pastDate = Instant.now().minusSeconds(3600).toString()
        mockMvc.perform(
            post("/loans").with(student("student-edge-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"equipmentId":$equipmentId,"estimatedReturnDate":"$pastDate"}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    @Order(7)
    fun `staff rejects a pending loan and the equipment becomes available again`() {
        val loanBody = mockMvc.perform(
            post("/loans").with(student("student-edge-2"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"equipmentId":$equipmentId}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val loanId = objectMapper.readTree(loanBody)["id"].asLong()

        mockMvc.perform(
            patch("/loans/$loanId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"REJECTED"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("REJECTED"))

        mockMvc.perform(get("/equipment/$equipmentId"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
    }

    @Test
    @Order(8)
    fun `a student cannot cancel another student's loan`() {
        val loanBody = mockMvc.perform(
            post("/loans").with(student("student-owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"equipmentId":$equipmentId}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val loanId = objectMapper.readTree(loanBody)["id"].asLong()

        mockMvc.perform(delete("/loans/$loanId").with(student("student-not-owner")))
            .andExpect(status().isForbidden)

        // The real owner can cancel it.
        mockMvc.perform(delete("/loans/$loanId").with(student("student-owner")))
            .andExpect(status().isNoContent)
    }

    @Test
    @Order(9)
    fun `staff deletes equipment that has no loan history`() {
        // equipmentId already has a REJECTED loan in its history (previous test),
        // so a fresh one is needed here to exercise the successful-delete path.
        val freshEquipmentBody = mockMvc.perform(
            post("/equipment").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"categoryId":$categoryId,"name":"Disposable Equipment","serialNumber":"EDGE-CASE-002"}""")
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val freshEquipmentId = objectMapper.readTree(freshEquipmentBody)["id"].asLong()

        mockMvc.perform(delete("/equipment/$freshEquipmentId").with(staff()))
            .andExpect(status().isNoContent)
    }

    @Test
    @Order(10)
    fun `creating equipment with a duplicate serial number returns 409`() {
        val body = """{"categoryId":$categoryId,"name":"Duplicate Serial Equipment A","serialNumber":"EDGE-CASE-DUP-001"}"""
        mockMvc.perform(post("/equipment").with(staff()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated)

        val duplicateBody = """{"categoryId":$categoryId,"name":"Duplicate Serial Equipment B","serialNumber":"EDGE-CASE-DUP-001"}"""
        mockMvc.perform(post("/equipment").with(staff()).contentType(MediaType.APPLICATION_JSON).content(duplicateBody))
            .andExpect(status().isConflict)
    }
}
