package com.ss.rs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures this application as an OAuth2 Resource Server that validates JWT access tokens.
 *
 * <p><b>Purpose:</b> Protects REST API endpoints by validating JWT tokens issued by the
 * Authorization Server. All incoming requests must include a valid JWT Bearer token
 * in the Authorization header.</p>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Stateless session management (no server-side sessions)</li>
 *   <li>JWT validation using public keys from the Authorization Server's JWK Set endpoint</li>
 *   <li>Method-level security enabled via @PreAuthorize annotations</li>
 *   <li>Health endpoint exposed for monitoring</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity // enables method-level security annotations: @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
public class ResourceConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }
}
