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

/**
 * Recorre el flujo de negocio completo via HTTP real (MockMvc + Spring
 * Security real, JwtDecoder mockeado): categoria -> equipo -> solicitar
 * prestamo -> aprobar -> registrar incidencia -> consultar/editar/eliminar.
 * Ademas de validar el comportamiento de punta a punta, ejercita
 * controllers y mappers que los tests unitarios de servicio no tocan.
 *
 * @TestMethodOrder porque cada paso depende de los ids creados en el
 * anterior (mismo estilo de un flujo real de demo en Postman).
 */
@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:sigpelflowdb;DB_CLOSE_DELAY=-1",
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
        private var categoryId: Long = 0
        private var equipmentId: Long = 0
        private var loanId: Long = 0
        private var incidentId: Long = 0
    }

    private fun staff() = jwt()
        .jwt { it.subject("staff-1") }
        .authorities(SimpleGrantedAuthority("ROLE_ENCARGADO"))

    private fun student() = jwt()
        .jwt { it.subject("student-1") }
        .authorities(SimpleGrantedAuthority("ROLE_ESTUDIANTE"))

    @Test
    @Order(1)
    fun `01 - staff creates a category`() {
        val body = mockMvc.perform(
            post("/categories").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Flow Test Category"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Flow Test Category"))
            .andReturn().response.contentAsString
        categoryId = objectMapper.readTree(body)["id"].asLong()
    }

    @Test
    @Order(2)
    fun `02 - anyone lists categories`() {
        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").exists())
    }

    @Test
    @Order(3)
    fun `03 - staff renames the category`() {
        mockMvc.perform(
            patch("/categories/$categoryId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Flow Test Category Renamed"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Flow Test Category Renamed"))
    }

    @Test
    @Order(4)
    fun `04 - staff creates equipment in the category`() {
        val body = mockMvc.perform(
            post("/equipment").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"categoryId":$categoryId,"name":"Flow Test Equipment","description":"integration test"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andReturn().response.contentAsString
        equipmentId = objectMapper.readTree(body)["id"].asLong()
    }

    @Test
    @Order(5)
    fun `05 - anyone gets the equipment by id`() {
        mockMvc.perform(get("/equipment/$equipmentId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.categoryName").value("Flow Test Category Renamed"))
    }

    @Test
    @Order(6)
    fun `06 - anyone lists equipment filtered by category and status`() {
        mockMvc.perform(get("/equipment").param("categoryId", categoryId.toString()).param("status", "AVAILABLE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(equipmentId))
    }

    @Test
    @Order(7)
    fun `07 - student requests a loan for the equipment`() {
        val body = mockMvc.perform(
            post("/loans").with(student())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"equipmentId":$equipmentId}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().response.contentAsString
        loanId = objectMapper.readTree(body)["id"].asLong()
    }

    @Test
    @Order(8)
    fun `08 - equipment is now LOANED`() {
        mockMvc.perform(get("/equipment/$equipmentId"))
            .andExpect(jsonPath("$.status").value("LOANED"))
    }

    @Test
    @Order(9)
    fun `09 - student lists their own loans`() {
        mockMvc.perform(get("/loans/me").with(student()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(loanId))
    }

    @Test
    @Order(10)
    fun `10 - staff lists every loan`() {
        mockMvc.perform(get("/loans").with(staff()))
            .andExpect(status().isOk)
    }

    @Test
    @Order(11)
    fun `11 - staff approves the loan`() {
        mockMvc.perform(
            patch("/loans/$loanId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"APPROVED","comment":"Approved in integration test"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))
    }

    @Test
    @Order(12)
    fun `12 - staff registers an incident for the loan`() {
        val body = mockMvc.perform(
            post("/incidents").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"loanId":$loanId,"type":"DAMAGE","description":"Scratch on the case"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("DAMAGE"))
            .andReturn().response.contentAsString
        incidentId = objectMapper.readTree(body)["id"].asLong()
    }

    @Test
    @Order(13)
    fun `13 - staff registers a second incident for the same loan`() {
        mockMvc.perform(
            post("/incidents").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"loanId":$loanId,"type":"DELAY"}""")
        ).andExpect(status().isCreated)
    }

    @Test
    @Order(14)
    fun `14 - staff gets and updates the incident`() {
        mockMvc.perform(get("/incidents/$incidentId").with(staff()))
            .andExpect(status().isOk)

        mockMvc.perform(
            patch("/incidents/$incidentId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"loanId":$loanId,"type":"DAMAGE","description":"Updated description"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description").value("Updated description"))
    }

    @Test
    @Order(15)
    fun `15 - staff marks the loan as returned`() {
        mockMvc.perform(
            patch("/loans/$loanId").with(staff())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"RETURNED"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RETURNED"))

        mockMvc.perform(get("/equipment/$equipmentId"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
    }

    @Test
    @Order(16)
    fun `16 - staff deletes the incident, but cannot delete equipment with loan history`() {
        mockMvc.perform(delete("/incidents/$incidentId").with(staff()))
            .andExpect(status().isNoContent)

        // The equipment still has a (returned) loan pointing to it: deleting it
        // would break that history, so the FK violation is translated to 409
        // instead of failing with a raw 500.
        mockMvc.perform(delete("/equipment/$equipmentId").with(staff()))
            .andExpect(status().isConflict)
    }

    @Test
    @Order(17)
    fun `17 - staff cannot delete a category that still has equipment`() {
        mockMvc.perform(delete("/categories/$categoryId").with(staff()))
            .andExpect(status().isConflict)
    }
}
