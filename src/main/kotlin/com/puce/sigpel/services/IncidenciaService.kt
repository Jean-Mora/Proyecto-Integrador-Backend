package com.puce.sigpel.services

import com.puce.sigpel.dto.IncidenciaRequest
import com.puce.sigpel.entities.Incidencia
import com.puce.sigpel.exceptions.IncidenciaYaRegistradaException
import com.puce.sigpel.repositories.IncidenciaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IncidenciaService(
    private val incidenciaRepository: IncidenciaRepository,
    private val prestamoService: PrestamoService
) {
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
}
