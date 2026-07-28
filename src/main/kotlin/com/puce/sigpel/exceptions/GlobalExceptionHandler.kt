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
 * Traduce las excepciones de negocio a respuestas HTTP consistentes:
 * 400 validaciones, 403 rol/propiedad incorrectos, 404 recurso inexistente,
 * 409 conflictos de estado o de concurrencia (optimistic locking).
 * El 401 (sin token o token invalido) lo maneja directamente Spring Security
 * antes de llegar aqui.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.NOT_FOUND, ex.message, req)

    @ExceptionHandler(ForbiddenOperationException::class, AccessDeniedException::class)
    fun handleForbidden(ex: Exception, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.FORBIDDEN, ex.message ?: "No tienes permiso para esta operacion", req)

    @ExceptionHandler(
        EquipoNoDisponibleException::class,
        IncidenciaYaRegistradaException::class,
        ObjectOptimisticLockingFailureException::class
    )
    fun handleConflict(ex: Exception, req: HttpServletRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, ex.message ?: "El recurso fue modificado por otra solicitud, intenta de nuevo", req)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return build(HttpStatus.BAD_REQUEST, message, req)
    }

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
