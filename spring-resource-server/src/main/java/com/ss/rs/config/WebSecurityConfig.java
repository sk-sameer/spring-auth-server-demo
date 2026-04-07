package com.ss.rs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
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
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            Converter<Jwt, AbstractAuthenticationToken> jwtAuthConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/private_key_jwt/jwks").permitAll() // private_key_jwt client authentication require public key retrieval from this endpoint, so it must be publicly accessible
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .oauth2Client(Customizer.withDefaults()); // enables OAuth2 client support for making outbound requests with access tokens (service-to-service calls)

        return http.build();
    }

}
