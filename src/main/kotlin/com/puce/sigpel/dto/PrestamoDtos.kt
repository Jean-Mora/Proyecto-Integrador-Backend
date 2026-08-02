package com.puce.sigpel.dto

import com.puce.sigpel.entities.EstadoPrestamo
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class PrestamoRequest(
    @field:NotNull(message = "El equipo es obligatorio")
    val equipoId: Long,

    val fechaDevolucionEstimada: Instant? = null
)

data class PrestamoEstadoRequest(
    @field:NotNull(message = "El estado es obligatorio")
    val estado: EstadoPrestamo,

    @field:Size(max = 255, message = "El comentario no puede superar 255 caracteres")
    val comentario: String? = null
)

data class PrestamoResponse(
    val id: Long,
    val equipoId: Long,
    val equipoNombre: String,
    val estudianteUser: String,
    val fechaSolicitud: Instant,
    val fechaDevolucionEstimada: Instant?,
    val fechaDevolucionReal: Instant?,
    val estado: EstadoPrestamo,
    val comentario: String?
)
