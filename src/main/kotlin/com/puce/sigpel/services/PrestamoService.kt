package com.puce.sigpel.services

import com.puce.sigpel.config.CurrentUser
import com.puce.sigpel.dto.PrestamoEstadoRequest
import com.puce.sigpel.dto.PrestamoRequest
import com.puce.sigpel.entities.EstadoEquipo
import com.puce.sigpel.entities.EstadoPrestamo
import com.puce.sigpel.entities.Prestamo
import com.puce.sigpel.exceptions.EquipoNoDisponibleException
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.PrestamoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PrestamoService(
    private val prestamoRepository: PrestamoRepository,
    private val equipoService: EquipoService
) {
    @Transactional(readOnly = true)
    fun obtener(id: Long): Prestamo =
        prestamoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Prestamo $id no encontrado") }

    @Transactional(readOnly = true)
    fun listarMios(): List<Prestamo> =
        prestamoRepository.findByEstudianteUser(CurrentUser.username())

    /**
     * Solicita un prestamo dentro de una unica transaccion: valida que el
     * equipo este DISPONIBLE, lo marca como PRESTADO y crea el prestamo.
     *
     * Desafio resuelto aqui: si dos estudiantes solicitan el mismo equipo al
     * mismo tiempo, ambas transacciones leen la misma "version" del equipo.
     * La primera en confirmar (commit) gana; cuando la segunda intenta guardar,
     * Hibernate detecta que la version cambio y lanza
     * ObjectOptimisticLockingFailureException, que el GlobalExceptionHandler
     * traduce a 409 Conflict en vez de dejar datos inconsistentes.
     */
    fun solicitar(request: PrestamoRequest): Prestamo {
        val equipo = equipoService.obtener(request.equipoId)
        if (equipo.estado != EstadoEquipo.DISPONIBLE) {
            throw EquipoNoDisponibleException("El equipo '${equipo.nombre}' no esta disponible")
        }
        equipo.estado = EstadoEquipo.PRESTADO

        val prestamo = Prestamo(
            equipo = equipo,
            estudianteUser = CurrentUser.username(),
            fechaDevolucionEstimada = request.fechaDevolucionEstimada
        )
        return prestamoRepository.save(prestamo)
    }

    /** ENCARGADO aprueba o rechaza un prestamo pendiente. */
    fun cambiarEstado(id: Long, request: PrestamoEstadoRequest): Prestamo {
        val prestamo = obtener(id)
        prestamo.estado = request.estado
        request.comentario?.let { prestamo.comentario = it }

        when (request.estado) {
            EstadoPrestamo.RECHAZADO -> prestamo.equipo.estado = EstadoEquipo.DISPONIBLE
            else -> Unit
        }
        return prestamoRepository.save(prestamo)
    }
}
