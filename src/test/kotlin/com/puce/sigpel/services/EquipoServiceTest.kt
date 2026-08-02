package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipoRequest
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.entities.EstadoEquipo
import com.puce.sigpel.repositories.EquipoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EquipoServiceTest {

    private val equipoRepository = mockk<EquipoRepository>()
    private val categoriaEquipoService = mockk<CategoriaEquipoService>()
    private lateinit var equipoService: EquipoService

    @BeforeEach
    fun setUp() {
        equipoService = EquipoService(equipoRepository, categoriaEquipoService)
    }

    @Test
    fun `crear asocia el equipo a la categoria existente y queda DISPONIBLE por defecto`() {
        val categoria = CategoriaEquipo(id = 1L, nombre = "Electronica")
        every { categoriaEquipoService.obtener(1L) } returns categoria
        val savedSlot = slot<Equipo>()
        every { equipoRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        equipoService.crear(EquipoRequest(categoriaId = 1L, nombre = "Osciloscopio"))

        assertEquals("Osciloscopio", savedSlot.captured.nombre)
        assertEquals(EstadoEquipo.DISPONIBLE, savedSlot.captured.estado)
        assertEquals(categoria, savedSlot.captured.categoria)
    }
}
