package com.puce.labloan.controllers

import com.puce.labloan.dto.IncidenciaRequest
import com.puce.labloan.dto.IncidenciaResponse
import com.puce.labloan.mappers.toResponse
import com.puce.labloan.services.IncidenciaService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/incidencias")
@PreAuthorize("hasRole('ENCARGADO')")
class IncidenciaController(
    private val incidenciaService: IncidenciaService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun registrar(@Valid @RequestBody request: IncidenciaRequest): IncidenciaResponse =
        incidenciaService.registrar(request).toResponse()

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): IncidenciaResponse =
        incidenciaService.obtener(id).toResponse()

    @PatchMapping("/{id}")
    fun actualizar(@PathVariable id: Long, @Valid @RequestBody request: IncidenciaRequest): IncidenciaResponse =
        incidenciaService.actualizar(id, request).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun eliminar(@PathVariable id: Long) = incidenciaService.eliminar(id)
}
