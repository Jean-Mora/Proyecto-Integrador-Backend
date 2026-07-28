package com.puce.sigpel.dto

import com.puce.sigpel.entities.TipoIncidencia
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class IncidenciaRequest(
    @field:NotNull(message = "El prestamo es obligatorio")
    val prestamoId: Long,

    @field:NotNull(message = "El tipo es obligatorio")
    val tipo: TipoIncidencia,

    @field:Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
    val descripcion: String? = null
)

data class IncidenciaResponse(
    val id: Long,
    val prestamoId: Long,
    val tipo: TipoIncidencia,
    val descripcion: String?,
    val fechaReporte: Instant
)
