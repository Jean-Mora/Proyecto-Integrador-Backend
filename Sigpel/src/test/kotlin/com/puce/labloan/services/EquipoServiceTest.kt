package com.puce.labloan.services

import com.puce.labloan.dto.EquipoRequest
import com.puce.labloan.entities.CategoriaEquipo
import com.puce.labloan.entities.Equipo
import com.puce.labloan.entities.EstadoEquipo
import com.puce.labloan.exceptions.ResourceNotFoundException
import com.puce.labloan.repositories.EquipoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

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

    @Test
    fun `obtener lanza ResourceNotFoundException si el equipo no existe`() {
        every { equipoRepository.findById(99L) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            equipoService.obtener(99L)
        }
    }

    @Test
    fun `eliminar borra el equipo cuando existe`() {
        val equipo = Equipo(id = 2L, categoria = CategoriaEquipo(id = 1L, nombre = "Electronica"), nombre = "Multimetro")
        every { equipoRepository.findById(2L) } returns Optional.of(equipo)
        every { equipoRepository.delete(equipo) } returns Unit

        equipoService.eliminar(2L)

        verify(exactly = 1) { equipoRepository.delete(equipo) }
    }
}
