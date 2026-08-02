package com.puce.sigpel.dto

import com.puce.sigpel.entities.EstadoEquipo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class EquipoRequest(
    @field:NotNull(message = "La categoria es obligatoria")
    val categoriaId: Long,

    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    val nombre: String,

    @field:Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
    val descripcion: String? = null
)

data class EquipoResponse(
    val id: Long,
    val categoriaId: Long,
    val categoriaNombre: String,
    val nombre: String,
    val estado: EstadoEquipo,
    val descripcion: String?
)
