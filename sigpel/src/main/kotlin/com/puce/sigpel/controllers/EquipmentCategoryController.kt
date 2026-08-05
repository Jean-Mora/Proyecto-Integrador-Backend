package com.puce.sigpel.controllers

import com.puce.sigpel.dto.EquipmentCategoryRequest
import com.puce.sigpel.dto.EquipmentCategoryResponse
import com.puce.sigpel.mappers.toResponse
import com.puce.sigpel.services.EquipmentCategoryService
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
@RequestMapping("/categories")
class EquipmentCategoryController(
    private val equipmentCategoryService: EquipmentCategoryService
) {
    @GetMapping
    fun list(): List<EquipmentCategoryResponse> =
        equipmentCategoryService.list().map { it.toResponse() }

    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: EquipmentCategoryRequest): EquipmentCategoryResponse =
        equipmentCategoryService.create(request).toResponse()

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: EquipmentCategoryRequest): EquipmentCategoryResponse =
        equipmentCategoryService.update(id, request).toResponse()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = equipmentCategoryService.delete(id)
}
