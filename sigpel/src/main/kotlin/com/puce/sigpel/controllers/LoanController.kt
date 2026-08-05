package com.puce.sigpel.controllers

import com.puce.sigpel.dto.LoanRequest
import com.puce.sigpel.dto.LoanResponse
import com.puce.sigpel.dto.LoanStatusRequest
import com.puce.sigpel.mappers.toResponse
import com.puce.sigpel.services.LoanService
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
@RequestMapping("/loans")
class LoanController(
    private val loanService: LoanService
) {
    @PostMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @ResponseStatus(HttpStatus.CREATED)
    fun request(@Valid @RequestBody request: LoanRequest): LoanResponse =
        loanService.request(request).toResponse()

    @GetMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    fun myLoans(): List<LoanResponse> =
        loanService.listMine().map { it.toResponse() }

    /** Allows STAFF (ENCARGADO) to see every loan in the system (HU-22). */
    @GetMapping
    @PreAuthorize("hasRole('ENCARGADO')")
    fun listAll(): List<LoanResponse> =
        loanService.listAll().map { it.toResponse() }

    /** Approve, reject or mark as returned: the new status goes in the body. */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ENCARGADO')")
    fun changeStatus(@PathVariable id: Long, @Valid @RequestBody request: LoanStatusRequest): LoanResponse =
        loanService.changeStatus(id, request).toResponse()

    /** Authorization by role (ESTUDIANTE) + by ownership (loan owner, validated in the service). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable id: Long) = loanService.cancel(id)
}
