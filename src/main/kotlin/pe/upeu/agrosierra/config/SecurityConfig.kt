package pe.upeu.agrosierra.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import pe.upeu.agrosierra.web.dto.ErrorResponse
import java.time.Instant

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtFilter: JwtAuthenticationFilter,
        objectMapper: ObjectMapper,
    ): SecurityFilterChain {
        fun responder(response: HttpServletResponse, request: HttpServletRequest, status: Int, message: String) {
            response.status = status
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(response.outputStream, ErrorResponse(
                timestamp = Instant.now(), status = status,
                error = if (status == 401) "Unauthorized" else "Forbidden",
                message = message, path = request.requestURI,
            ))
        }
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/login", "/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, _ ->
                    responder(response, request, 401, "Token ausente, alterado o vencido")
                }
                it.accessDeniedHandler { request, response, _ ->
                    responder(response, request, 403, "No posee el privilegio requerido")
                }
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
