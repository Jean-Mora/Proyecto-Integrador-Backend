package com.puce.sigpel.controllers

import com.puce.sigpel.dto.IncidenciaRequest
import com.puce.sigpel.dto.IncidenciaResponse
import com.puce.sigpel.mappers.toResponse
import com.puce.sigpel.services.IncidenciaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
}
