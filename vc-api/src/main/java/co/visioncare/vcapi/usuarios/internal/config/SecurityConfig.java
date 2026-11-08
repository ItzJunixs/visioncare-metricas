package co.visioncare.vcapi.usuarios.internal.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.visioncare.vcapi.usuarios.internal.security.JwtAuthenticationFilter;

/**
 * Stateless JWT security (ADR-004). A missing or invalid token answers 401; a valid token
 * whose role may not perform the operation answers 403. The role table mirrors
 * vc-docs/07-api/rest-conventions.md. CORS admits only the portal's origin.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          @Value("${app.cors.allowed-origins}") String origins) {
        this.jwtFilter = jwtFilter;
        this.allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).toList();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) ->
                        writeError(response, 401, "UNAUTHORIZED", "Authentication is required"))
                .accessDeniedHandler((request, response, e) ->
                        writeError(response, 403, "FORBIDDEN", "Your role may not perform this operation")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/frames").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/frames/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/frames/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/frames/*/min-stock").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/work-orders").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/work-orders/*/approve").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.PATCH, "/api/work-orders/*/status").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/invoices/*/payments").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeError(jakarta.servlet.http.HttpServletResponse response, int status,
                                   String code, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }

    private CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
