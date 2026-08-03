package com.puce.sigpel.mappers

import com.puce.sigpel.dto.CategoriaEquipoResponse
import com.puce.sigpel.dto.EquipoResponse
import com.puce.sigpel.dto.IncidenciaResponse
import com.puce.sigpel.dto.PrestamoResponse
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.entities.Incidencia
import com.puce.sigpel.entities.Prestamo

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

fun Prestamo.toResponse() = PrestamoResponse(
    id = requireNotNull(id),
    equipoId = requireNotNull(equipo.id),
    equipoNombre = equipo.nombre,
    estudianteUser = estudianteUser,
    fechaSolicitud = fechaSolicitud,
    fechaDevolucionEstimada = fechaDevolucionEstimada,
    fechaDevolucionReal = fechaDevolucionReal,
    estado = estado,
    comentario = comentario
)

fun Incidencia.toResponse() = IncidenciaResponse(
    id = requireNotNull(id),
    prestamoId = requireNotNull(prestamo.id),
    tipo = tipo,
    descripcion = descripcion,
    fechaReporte = fechaReporte
)
