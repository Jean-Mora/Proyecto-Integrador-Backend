package com.puce.sigpel.controllers

import com.puce.sigpel.dto.CategoriaEquipoRequest
import com.puce.sigpel.dto.CategoriaEquipoResponse
import com.puce.sigpel.mappers.toResponse
import com.puce.sigpel.services.CategoriaEquipoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categorias")
class CategoriaEquipoController(
    private val categoriaEquipoService: CategoriaEquipoService
) {
    @GetMapping
    fun listar(): List<CategoriaEquipoResponse> =
        categoriaEquipoService.listar().map { it.toResponse() }

    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO')")
    @ResponseStatus(HttpStatus.CREATED)
    fun crear(@Valid @RequestBody request: CategoriaEquipoRequest): CategoriaEquipoResponse =
        categoriaEquipoService.crear(request).toResponse()

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    fun editar(@PathVariable id: Long, @Valid @RequestBody request: CategoriaEquipoRequest): CategoriaEquipoResponse =
        categoriaEquipoService.editar(id, request).toResponse()
}
