package com.puce.sigpel.dto

import com.puce.sigpel.entities.EstadoPrestamo
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class PrestamoRequest(
    @field:NotNull(message = "El equipo es obligatorio")
    val equipoId: Long,

    val fechaDevolucionEstimada: Instant? = null
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
