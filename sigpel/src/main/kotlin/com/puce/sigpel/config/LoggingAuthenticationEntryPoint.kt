package com.puce.sigpel.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/**
 * Se dispara cuando la peticion no trae un JWT valido en un endpoint que lo
 * exige. Esto ocurre ANTES de que RequestLoggingFilter llegue a ejecutarse
 * (Spring Security corta la cadena aqui), asi que es el unico lugar donde se
 * puede dejar registro del 401 en el mismo formato estandar de logging.
 */
class LoggingAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val log = LoggerFactory.getLogger(LoggingAuthenticationEntryPoint::class.java)

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        log.warn("event=http.response | msg=401 ${request.method} ${request.requestURI} | reason=\"${authException.message}\"")
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write(
            """{"status":401,"error":"Unauthorized","message":"Missing or invalid JWT","path":"${request.requestURI}"}"""
        )
    }
}
