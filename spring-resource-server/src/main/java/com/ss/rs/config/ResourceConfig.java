package com.ss.rs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
/**
 * Configures this application as an OAuth2 Resource Server that validates JWT access tokens.
 *
 * <p><b>Purpose:</b> Protects REST API endpoints by validating JWT tokens issued by the
 * Authorization Server (spring-auth-server). All incoming requests must include a valid
 * JWT Bearer token in the Authorization header.</p>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>All endpoints require authentication (no public endpoints)</li>
 *   <li>JWT validation using public keys from the Authorization Server's JWK Set endpoint</li>
 *   <li>Method-level security enabled via @PreAuthorize, @PostAuthorize, @Secured annotations</li>
 * </ul>
 *
 * <p><b>Token Validation:</b> Tokens are verified against the issuer configured in application.yml
 * (spring.security.oauth2.resourceserver.jwt.issuer-uri). The resource server automatically
 * fetches the public keys from the Authorization Server to validate token signatures.</p>
 */
@Configuration
@EnableMethodSecurity
// enables method-level security annotations: @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
public class ResourceConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );
        return http.build();
    }

}
