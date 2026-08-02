package com.puce.sigpel.controllers

import com.puce.sigpel.dto.PrestamoRequest
import com.puce.sigpel.dto.PrestamoResponse
import com.puce.sigpel.mappers.toResponse
import com.puce.sigpel.services.PrestamoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/prestamos")
class PrestamoController(
    private val prestamoService: PrestamoService
) {
    @PostMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @ResponseStatus(HttpStatus.CREATED)
    fun solicitar(@Valid @RequestBody request: PrestamoRequest): PrestamoResponse =
        prestamoService.solicitar(request).toResponse()

    @GetMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    fun misPrestamos(): List<PrestamoResponse> =
        prestamoService.listarMios().map { it.toResponse() }
}
