package com.puce.sigpel.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Se registra justo despues del filtro de autenticacion JWT (ver
 * SecurityConfig.addFilterAfter), para que el claim "sub" del token ya este
 * disponible en el SecurityContext. Dos responsabilidades:
 *  1. Pone "sub" en el MDC, que el patron de logging (application.yml) usa
 *     en cada linea via %X{sub}.
 *  2. Deja una linea de entrada (event=http.request) y una de salida
 *     (event=http.response con el codigo HTTP) por cada peticion, para que
 *     nunca pase un request sin dejar rastro.
 */
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        MDC.put("sub", currentSub())
        try {
            log.info("event=http.request | msg=${request.method} ${request.requestURI}")
            filterChain.doFilter(request, response)
        } finally {
            log.info("event=http.response | msg=${response.status} ${request.method} ${request.requestURI}")
            MDC.remove("sub")
        }
    }

    private fun currentSub(): String {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is Jwt) principal.subject else "anonimo"
    }
}
