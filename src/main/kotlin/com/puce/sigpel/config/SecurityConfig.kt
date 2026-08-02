package com.puce.sigpel.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

/**
 * Autenticacion: Cognito emite y firma el JWT; Spring Security lo valida en
 * cada endpoint usando el issuer-uri configurado en application.yml.
 *
 * El claim "cognito:groups" (ENCARGADO / ESTUDIANTE) se mapea a authorities
 * ROLE_*, que se usaran mas adelante para autorizacion por rol.
 */
@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = cognitoJwtAuthenticationConverter()
                }
            }
        }
        return http.build()
    }

    private fun cognitoJwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("cognito:groups")
            setAuthorityPrefix("ROLE_")
        }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        }
    }
}
