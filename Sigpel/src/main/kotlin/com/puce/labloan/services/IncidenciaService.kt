package com.puce.labloan.services

import com.puce.labloan.dto.IncidenciaRequest
import com.puce.labloan.entities.Incidencia
import com.puce.labloan.exceptions.IncidenciaYaRegistradaException
import com.puce.labloan.exceptions.ResourceNotFoundException
import com.puce.labloan.repositories.IncidenciaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IncidenciaService(
    private val incidenciaRepository: IncidenciaRepository,
    private val prestamoService: PrestamoService
) {
    @Transactional(readOnly = true)
    fun obtener(id: Long): Incidencia =
        incidenciaRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Incidencia $id no encontrada") }

    /** La relacion prestamos-incidencias es 1:1: un prestamo no puede tener dos. */
    fun registrar(request: IncidenciaRequest): Incidencia {
        val prestamo = prestamoService.obtener(request.prestamoId)
        if (incidenciaRepository.existsByPrestamoId(request.prestamoId)) {
            throw IncidenciaYaRegistradaException(
                "El prestamo ${request.prestamoId} ya tiene una incidencia registrada"
            )
        }
        val incidencia = Incidencia(
            prestamo = prestamo,
            tipo = request.tipo,
            descripcion = request.descripcion
        )
        return incidenciaRepository.save(incidencia)
    }

    fun actualizar(id: Long, request: IncidenciaRequest): Incidencia {
        val incidencia = obtener(id)
        incidencia.tipo = request.tipo
        incidencia.descripcion = request.descripcion
        return incidenciaRepository.save(incidencia)
    }

    fun eliminar(id: Long) {
        incidenciaRepository.delete(obtener(id))
    }
}
