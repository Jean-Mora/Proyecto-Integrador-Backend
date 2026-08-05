package com.puce.sigpel.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates business exceptions into consistent HTTP responses:
 * 400 validation, 403 wrong role/ownership, 404 resource not found,
 * 409 status or concurrency conflicts (optimistic locking).
 * 401 (no token or invalid token) is handled directly by Spring Security
 * before reaching this class.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.NOT_FOUND, ex.message, req)

    @ExceptionHandler(ForbiddenOperationException::class, AccessDeniedException::class)
    fun handleForbidden(ex: Exception, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.FORBIDDEN, ex.message ?: "You do not have permission for this operation", req)

    @ExceptionHandler(
        EquipmentNotAvailableException::class,
        DuplicateResourceException::class,
        ObjectOptimisticLockingFailureException::class
    )
    fun handleConflict(ex: Exception, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, ex.message ?: "The resource was modified by another request, please try again", req)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return build(HttpStatus.BAD_REQUEST, message, req)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, ex.message, req)

    private fun build(status: HttpStatus, message: String?, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = req.requestURI
            )
        )
}
