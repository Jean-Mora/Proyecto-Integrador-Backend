package com.puce.sigpel.services

import com.puce.sigpel.config.CurrentUser
import com.puce.sigpel.dto.PrestamoEstadoRequest
import com.puce.sigpel.dto.PrestamoRequest
import com.puce.sigpel.entities.EstadoEquipo
import com.puce.sigpel.entities.EstadoPrestamo
import com.puce.sigpel.entities.Prestamo
import com.puce.sigpel.entities.PrestamoAuditoria // <-- 1. Importar la entidad de auditoría
import com.puce.sigpel.exceptions.EquipoNoDisponibleException
import com.puce.sigpel.exceptions.ForbiddenOperationException
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.PrestamoAuditoriaRepository // <-- 2. Importar el repositorio de auditoría
import com.puce.sigpel.repositories.PrestamoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class PrestamoService(
    private val prestamoRepository: PrestamoRepository,
    private val equipoService: EquipoService,
    private val prestamoAuditoriaRepository: PrestamoAuditoriaRepository // <-- 3. Inyectarlo aquí en el constructor
) {
    @Transactional(readOnly = true)
    fun obtener(id: Long): Prestamo =
        prestamoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Prestamo $id no encontrado") }

    @Transactional(readOnly = true)
    fun listarMios(): List<Prestamo> =
        prestamoRepository.findByEstudianteUser(CurrentUser.username())

    /** Permite al ENCARGADO ver todos los préstamos del sistema (HU-22). */
    @Transactional(readOnly = true)
    open fun listarTodos(): List<Prestamo> =
        prestamoRepository.findAll()

    fun solicitar(request: PrestamoRequest): Prestamo {
        if (request.fechaDevolucionEstimada?.isBefore(Instant.now()) != false) {
            throw IllegalArgumentException("La fecha de devolución estimada debe ser posterior a la fecha actual")
        }

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

        // Guardamos el estado anterior para la auditoría (HU-25)
        val estadoAnteriorStr = prestamo.estado.name
        val estadoNuevoStr = request.estado.name

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

        val prestamoGuardado = prestamoRepository.save(prestamo)

        // --- HU-25: Registrar la auditoría del cambio de estado con fecha y usuario ---
        val auditoria = PrestamoAuditoria(
            prestamoId = prestamoGuardado.id!!,
            estadoAnterior = estadoAnteriorStr,
            estadoNuevo = estadoNuevoStr,
            modificadoPor = CurrentUser.username() // Obtiene automáticamente al ENCARGADO logueado
        )
        prestamoAuditoriaRepository.save(auditoria)

        return prestamoGuardado
    }

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