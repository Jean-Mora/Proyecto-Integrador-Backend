package com.pucetec.users.config

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
 * disponible en el SecurityContext. Pone "sub" en el MDC (usado por el
 * patron de logging en application.yaml via %X{sub}) y deja una linea de
 * entrada (event=http.request) y una de salida (event=http.response con el
 * codigo HTTP) por cada peticion.
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
