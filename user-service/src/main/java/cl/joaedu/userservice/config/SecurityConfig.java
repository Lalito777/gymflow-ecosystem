package cl.joaedu.userservice.config;

import cl.joaedu.userservice.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Seguridad real de user-service: dos roles del dominio (SOCIO, ADMIN), reglas por endpoint
 * y respuestas de error en el mismo formato JSON que el resto de la API (ErrorResponse),
 * en vez de las paginas HTML por defecto de Spring Security.
 *
 * - POST /api/users (registro de socio) es publico: cualquiera puede crear una cuenta.
 * - GET /api/users (listar todos los socios) requiere rol ADMIN: es informacion de otros usuarios.
 * - Swagger queda publico para poder mostrarlo en la defensa sin credenciales.
 * - Todo lo demas requiere autenticacion (HTTP Basic + BCrypt contra la BD real).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorizedEntryPoint()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                );
        return http.build();
    }

    /**
     * 401: no autenticado. Se devuelve en el mismo formato ErrorResponse que el resto de la API,
     * no la pantalla HTML por defecto de Spring Security.
     */
    private org.springframework.security.web.AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Autenticacion requerida",
                    request.getRequestURI()
            );
            objectMapper.writeValue(response.getWriter(), error);
        };
    }

    /**
     * 403: autenticado pero sin el rol requerido (ej. un SOCIO intentando listar todos los usuarios).
     */
    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "No tienes permisos para acceder a este recurso",
                    request.getRequestURI()
            );
            objectMapper.writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
