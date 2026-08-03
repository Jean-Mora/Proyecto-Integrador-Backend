package com.puce.sigpel.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoriaEquipoRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    val nombre: String
)

data class CategoriaEquipoResponse(
    val id: Long,
    val nombre: String
)
