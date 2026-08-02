package com.puce.sigpel.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
 * Autorizacion por rol: el claim "cognito:groups" (ENCARGADO / ESTUDIANTE) se
 * mapea a authorities ROLE_*. @EnableMethodSecurity habilita @PreAuthorize en
 * los controllers para exigir el rol correcto en cada endpoint sensible.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                // Catalogo publico: cualquiera puede consultar categorias.
                authorize(HttpMethod.GET, "/categorias", permitAll)
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
