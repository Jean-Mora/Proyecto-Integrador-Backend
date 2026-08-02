package com.puce.sigpel.mappers

import com.puce.sigpel.dto.CategoriaEquipoResponse
import com.puce.sigpel.entities.CategoriaEquipo

fun CategoriaEquipo.toResponse() = CategoriaEquipoResponse(
    id = requireNotNull(id),
    nombre = nombre
)
