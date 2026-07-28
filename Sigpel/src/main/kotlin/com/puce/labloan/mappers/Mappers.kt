package com.puce.labloan.mappers

import com.puce.labloan.dto.CategoriaEquipoResponse
import com.puce.labloan.dto.EquipoResponse
import com.puce.labloan.dto.IncidenciaResponse
import com.puce.labloan.dto.PrestamoResponse
import com.puce.labloan.entities.CategoriaEquipo
import com.puce.labloan.entities.Equipo
import com.puce.labloan.entities.Incidencia
import com.puce.labloan.entities.Prestamo

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
