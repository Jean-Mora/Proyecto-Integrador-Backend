package com.puce.labloan.controllers

import com.puce.labloan.dto.EquipoEstadoRequest
import com.puce.labloan.dto.EquipoRequest
import com.puce.labloan.dto.EquipoResponse
import com.puce.labloan.entities.EstadoEquipo
import com.puce.labloan.mappers.toResponse
import com.puce.labloan.services.EquipoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/equipos")
class EquipoController(
    private val equipoService: EquipoService
) {
    @GetMapping
    fun listar(@RequestParam(required = false) estado: EstadoEquipo?): List<EquipoResponse> =
        equipoService.listar(estado).map { it.toResponse() }

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): EquipoResponse =
        equipoService.obtener(id).toResponse()

    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO')")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody request: EquipoRequest): EquipoResponse =
        equipoService.crear(request).toResponse()

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    fun actualizarEstado(@PathVariable id: Long, @Valid @RequestBody request: EquipoEstadoRequest): EquipoResponse =
        equipoService.actualizarEstado(id, request).toResponse()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long) = equipoService.eliminar(id)
}
