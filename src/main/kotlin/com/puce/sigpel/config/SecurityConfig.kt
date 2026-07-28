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
 * cada endpoint privado usando el issuer-uri configurado en application.yml.
 *
 * Autorizacion por rol: el claim "cognito:groups" (ENCARGADO / ESTUDIANTE) se
 * mapea a authorities ROLE_*, usadas luego con @PreAuthorize en los controllers.
 *
 * Autorizacion por propiedad (ej. "solo el dueno del prestamo"): NO se puede
 * expresar aqui, se valida dentro de cada Service comparando el recurso contra
 * CurrentUser.username().
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
                // Catalogo publico: cualquiera puede consultar categorias y equipos.
                authorize(HttpMethod.GET, "/categorias", permitAll)
                authorize(HttpMethod.GET, "/equipos", permitAll)
                authorize(HttpMethod.GET, "/equipos/**", permitAll)
                // Todo lo demas (crear/editar/eliminar, prestamos, incidencias)
                // exige un JWT valido; el rol especifico se valida con @PreAuthorize.
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
