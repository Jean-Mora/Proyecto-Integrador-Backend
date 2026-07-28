package com.puce.labloan.services

import com.puce.labloan.dto.EquipoEstadoRequest
import com.puce.labloan.dto.EquipoRequest
import com.puce.labloan.entities.Equipo
import com.puce.labloan.entities.EstadoEquipo
import com.puce.labloan.exceptions.ResourceNotFoundException
import com.puce.labloan.repositories.EquipoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EquipoService(
    private val equipoRepository: EquipoRepository,
    private val categoriaEquipoService: CategoriaEquipoService
) {
    @Transactional(readOnly = true)
    fun listar(estado: EstadoEquipo?): List<Equipo> =
        if (estado != null) equipoRepository.findByEstado(estado) else equipoRepository.findAll()

    @Transactional(readOnly = true)
    fun obtener(id: Long): Equipo =
        equipoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Equipo $id no encontrado") }

    fun crear(request: EquipoRequest): Equipo {
        val categoria = categoriaEquipoService.obtener(request.categoriaId)
        val equipo = Equipo(
            categoria = categoria,
            nombre = request.nombre,
            descripcion = request.descripcion
        )
        return equipoRepository.save(equipo)
    }

    fun actualizarEstado(id: Long, request: EquipoEstadoRequest): Equipo {
        val equipo = obtener(id)
        equipo.estado = request.estado
        return equipoRepository.save(equipo)
    }

    fun eliminar(id: Long) {
        equipoRepository.delete(obtener(id))
    }
}
