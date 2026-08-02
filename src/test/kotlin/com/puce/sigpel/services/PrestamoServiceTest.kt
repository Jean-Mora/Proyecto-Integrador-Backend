package com.puce.sigpel.services

import com.puce.sigpel.config.CurrentUser
import com.puce.sigpel.dto.PrestamoRequest
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.entities.EstadoEquipo
import com.puce.sigpel.entities.Prestamo
import com.puce.sigpel.exceptions.EquipoNoDisponibleException
import com.puce.sigpel.repositories.PrestamoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrestamoServiceTest {

    private val prestamoRepository = mockk<PrestamoRepository>()
    private val equipoService = mockk<EquipoService>()
    private lateinit var prestamoService: PrestamoService
    private lateinit var equipoDisponible: Equipo

    @BeforeEach
    fun setUp() {
        prestamoService = PrestamoService(prestamoRepository, equipoService)
        mockkObject(CurrentUser)
        every { CurrentUser.username() } returns "estudiante01"

        equipoDisponible = Equipo(
            id = 1L,
            categoria = CategoriaEquipo(id = 1L, nombre = "Electronica"),
            nombre = "Multimetro",
            estado = EstadoEquipo.DISPONIBLE
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(CurrentUser)
    }

    @Test
    fun `solicitar crea el prestamo y marca el equipo como prestado cuando esta disponible`() {
        every { equipoService.obtener(1L) } returns equipoDisponible
        val savedSlot = slot<Prestamo>()
        every { prestamoRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        prestamoService.solicitar(PrestamoRequest(equipoId = 1L))

        assertEquals(EstadoEquipo.PRESTADO, equipoDisponible.estado)
        assertEquals("estudiante01", savedSlot.captured.estudianteUser)
        verify(exactly = 1) { prestamoRepository.save(any()) }
    }

    @Test
    fun `solicitar lanza EquipoNoDisponibleException si el equipo ya esta prestado`() {
        equipoDisponible.estado = EstadoEquipo.PRESTADO
        every { equipoService.obtener(1L) } returns equipoDisponible

        assertThrows(EquipoNoDisponibleException::class.java) {
            prestamoService.solicitar(PrestamoRequest(equipoId = 1L))
        }
        verify(exactly = 0) { prestamoRepository.save(any()) }
    }
}
