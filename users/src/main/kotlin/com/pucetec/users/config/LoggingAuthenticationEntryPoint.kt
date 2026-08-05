package com.pucetec.users.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/**
 * Se dispara cuando la peticion no trae un JWT valido. Ocurre ANTES de que
 * RequestLoggingFilter llegue a ejecutarse, asi que es el unico lugar donde
 * se puede dejar registro del 401 en el formato estandar de logging.
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
