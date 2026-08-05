package com.puce.sigpel.dto

import com.puce.sigpel.entities.LoanStatus
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class LoanRequest(
    @field:NotNull(message = "Equipment is required")
    val equipmentId: Long,

    val estimatedReturnDate: Instant? = null
)

data class LoanStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: LoanStatus,

    @field:Size(max = 255, message = "Comment cannot exceed 255 characters")
    val comment: String? = null
)

data class LoanResponse(
    val id: Long,
    val equipmentId: Long,
    val equipmentName: String,
    val studentUser: String,
    val requestDate: Instant,
    val estimatedReturnDate: Instant?,
    val actualReturnDate: Instant?,
    val status: LoanStatus,
    val comment: String?
)
