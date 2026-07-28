package com.puce.labloan.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Extrae el username del JWT de Cognito para las validaciones de autorizacion
 * por propiedad (ej. "solo el dueno del prestamo puede cancelarlo").
 */
object CurrentUser {
    fun username(): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: error("No hay un usuario autenticado en el contexto de seguridad")
        val jwt = authentication.principal as Jwt
        return jwt.getClaimAsString("cognito:username") ?: jwt.subject
    }
}
