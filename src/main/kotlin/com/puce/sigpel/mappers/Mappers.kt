package com.puce.sigpel.mappers

import com.puce.sigpel.dto.CategoriaEquipoResponse
import com.puce.sigpel.dto.EquipoResponse
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.entities.Equipo

fun CategoriaEquipo.toResponse() = CategoriaEquipoResponse(
    id = requireNotNull(id),
    nombre = nombre
)

fun Equipo.toResponse() = EquipoResponse(
    id = requireNotNull(id),
    categoriaId = requireNotNull(categoria.id),
    categoriaNombre = categoria.nombre,
    nombre = nombre,
    estado = estado,
    descripcion = descripcion
)
