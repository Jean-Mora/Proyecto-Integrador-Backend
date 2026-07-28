package com.puce.labloan.services

import com.puce.labloan.config.CurrentUser
import com.puce.labloan.dto.PrestamoEstadoRequest
import com.puce.labloan.dto.PrestamoRequest
import com.puce.labloan.entities.EstadoEquipo
import com.puce.labloan.entities.EstadoPrestamo
import com.puce.labloan.entities.Prestamo
import com.puce.labloan.exceptions.EquipoNoDisponibleException
import com.puce.labloan.exceptions.ForbiddenOperationException
import com.puce.labloan.exceptions.ResourceNotFoundException
import com.puce.labloan.repositories.PrestamoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

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

    /** ENCARGADO aprueba, rechaza o marca como devuelto un prestamo. */
    fun cambiarEstado(id: Long, request: PrestamoEstadoRequest): Prestamo {
        val prestamo = obtener(id)
        prestamo.estado = request.estado
        request.comentario?.let { prestamo.comentario = it }

        when (request.estado) {
            EstadoPrestamo.RECHAZADO -> prestamo.equipo.estado = EstadoEquipo.DISPONIBLE
            EstadoPrestamo.DEVUELTO -> {
                prestamo.equipo.estado = EstadoEquipo.DISPONIBLE
                prestamo.fechaDevolucionReal = Instant.now()
            }
            else -> Unit
        }
        return prestamoRepository.save(prestamo)
    }

    /**
     * Cancela un prestamo propio. Autorizacion por propiedad: se compara
     * prestamo.estudianteUser contra el username del JWT; si no coincide,
     * se lanza ForbiddenOperationException -> 403, aunque el rol (ESTUDIANTE)
     * sea correcto.
     */
    fun cancelar(id: Long) {
        val prestamo = obtener(id)
        if (prestamo.estudianteUser != CurrentUser.username()) {
            throw ForbiddenOperationException("No puedes cancelar un prestamo que no es tuyo")
        }
        if (prestamo.estado != EstadoPrestamo.PENDIENTE) {
            throw ForbiddenOperationException("Solo se puede cancelar un prestamo mientras esta PENDIENTE")
        }
        prestamo.equipo.estado = EstadoEquipo.DISPONIBLE
        prestamoRepository.delete(prestamo)
    }
}
