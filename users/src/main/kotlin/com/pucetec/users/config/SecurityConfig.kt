package com.pucetec.users.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Reglas de autorizacion por ruta: todo exige un token valido de Cognito.
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            // Convierte la app en un Resource Server: valida el JWT usando el
            // issuer-uri configurado en application.yaml (descarga el JWKS de Cognito
            // y verifica firma, expiracion e issuer del token).
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
                oauth2.authenticationEntryPoint(LoggingAuthenticationEntryPoint())
            }
            // CSRF no aplica a una API stateless que se autentica con Bearer token.
            .csrf { it.disable() }
            // Logging (Criterio 2): deja rastro de entrada/salida de cada peticion
            // con el "sub" del usuario en el MDC. Corre despues del filtro JWT para
            // que el SecurityContext ya este poblado.
            .addFilterAfter(RequestLoggingFilter(), BearerTokenAuthenticationFilter::class.java)

        return http.build()
    }
}
