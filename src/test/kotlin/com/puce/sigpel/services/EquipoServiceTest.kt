package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipoRequest
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.entities.EstadoEquipo
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.EquipoRepository
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

    // --- HU-23: listar() con filtros combinados ---

    @Test
    fun `listar sin filtros devuelve todos los equipos con categoria`() {
        val categoria = CategoriaEquipo(id = 1L, nombre = "Electronica")
        val equipo = Equipo(id = 1L, categoria = categoria, nombre = "Multimetro")
        every { equipoRepository.findAllWithCategoria() } returns listOf(equipo)

        val resultado = equipoService.listar(categoriaId = null, estado = null)

        assertEquals(1, resultado.size)
        verify(exactly = 1) { equipoRepository.findAllWithCategoria() }
        verify(exactly = 0) { categoriaEquipoService.obtener(any()) }
    }

    @Test
    fun `listar solo con estado usa findByEstadoWithCategoria y no valida categoria`() {
        val categoria = CategoriaEquipo(id = 1L, nombre = "Electronica")
        val equipo = Equipo(id = 1L, categoria = categoria, nombre = "Multimetro", estado = EstadoEquipo.MANTENIMIENTO)
        every { equipoRepository.findByEstadoWithCategoria(EstadoEquipo.MANTENIMIENTO) } returns listOf(equipo)

        val resultado = equipoService.listar(categoriaId = null, estado = EstadoEquipo.MANTENIMIENTO)

        assertEquals(1, resultado.size)
        verify(exactly = 1) { equipoRepository.findByEstadoWithCategoria(EstadoEquipo.MANTENIMIENTO) }
        verify(exactly = 0) { categoriaEquipoService.obtener(any()) }
    }

    @Test
    fun `listar solo con categoria valida su existencia y usa findByCategoriaAndEstado`() {
        val categoria = CategoriaEquipo(id = 1L, nombre = "Electronica")
        val equipo = Equipo(id = 1L, categoria = categoria, nombre = "Multimetro")
        every { categoriaEquipoService.obtener(1L) } returns categoria
        every { equipoRepository.findByCategoriaAndEstado(1L, null) } returns listOf(equipo)

        val resultado = equipoService.listar(categoriaId = 1L, estado = null)

        assertEquals(1, resultado.size)
        verify(exactly = 1) { categoriaEquipoService.obtener(1L) }
        verify(exactly = 1) { equipoRepository.findByCategoriaAndEstado(1L, null) }
    }

    @Test
    fun `listar con categoria y estado combinados`() {
        val categoria = CategoriaEquipo(id = 1L, nombre = "Electronica")
        val equipo = Equipo(id = 1L, categoria = categoria, nombre = "Multimetro", estado = EstadoEquipo.DISPONIBLE)
        every { categoriaEquipoService.obtener(1L) } returns categoria
        every { equipoRepository.findByCategoriaAndEstado(1L, EstadoEquipo.DISPONIBLE) } returns listOf(equipo)

        val resultado = equipoService.listar(categoriaId = 1L, estado = EstadoEquipo.DISPONIBLE)

        assertEquals(1, resultado.size)
        verify(exactly = 1) { equipoRepository.findByCategoriaAndEstado(1L, EstadoEquipo.DISPONIBLE) }
    }

    @Test
    fun `listar con categoria inexistente lanza ResourceNotFoundException y no consulta equipos`() {
        every { categoriaEquipoService.obtener(99L) } throws ResourceNotFoundException("Categoria 99 no encontrada")

        assertThrows(ResourceNotFoundException::class.java) {
            equipoService.listar(categoriaId = 99L, estado = null)
        }

        verify(exactly = 0) { equipoRepository.findByCategoriaAndEstado(any(), any()) }
        verify(exactly = 0) { equipoRepository.findAllWithCategoria() }
    }
}