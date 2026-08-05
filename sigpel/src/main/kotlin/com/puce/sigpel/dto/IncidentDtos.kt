package com.puce.sigpel.dto

import com.puce.sigpel.entities.IncidentType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class IncidentRequest(
    @field:NotNull(message = "Loan is required")
    val loanId: Long,

    @field:NotNull(message = "Type is required")
    val type: IncidentType,

    @field:Size(max = 255, message = "Description cannot exceed 255 characters")
    val description: String? = null
)

data class IncidentResponse(
    val id: Long,
    val loanId: Long,
    val type: IncidentType,
    val description: String?,
    val reportDate: Instant
)
