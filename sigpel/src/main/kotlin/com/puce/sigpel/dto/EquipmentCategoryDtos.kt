package com.puce.sigpel.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class EquipmentCategoryRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 50, message = "Name cannot exceed 50 characters")
    val name: String
)

data class EquipmentCategoryResponse(
    val id: Long,
    val name: String
)
