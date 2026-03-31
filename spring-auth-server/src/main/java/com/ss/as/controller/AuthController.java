package com.ss.as.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ss.as.config.AuthServerProperties;
import com.ss.as.exception.InvalidClientException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Custom authentication endpoint for direct JWT token generation.
 *
 * <p><b>WARNING:</b> This endpoint bypasses standard OAuth2 authorization flows and should
 * only be enabled for development/testing purposes. In production, use the standard
 * OAuth2 authorization code flow.</p>
 *
 * <p>Enable this controller by setting: {@code auth-server.dev-mode.enable-direct-login=true}</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth-server.dev-mode.enable-direct-login", havingValue = "true")
public class AuthController {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final AuthServerProperties properties;

    /**
     * Authenticates a user and issues a JWT access token.
     *
     * @param request Login request containing client and user credentials
     * @return Token response with access_token, token_type, and expires_in
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        // Validate client credentials
        RegisteredClient client = registeredClientRepository.findByClientId(request.clientId());
        if (client == null || !passwordEncoder.matches(request.clientSecret(), client.getClientSecret())) {
            throw new InvalidClientException("Invalid client credentials");
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Generate JWT token
        Instant now = Instant.now();
        long expiresInSeconds = 3600;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuerUri())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .subject(authentication.getName())
                .claim("scope", "openid profile email")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new TokenResponse("Bearer", token, expiresInSeconds);
    }

    /**
     * Request body for login endpoint.
     */
    public record LoginRequest(
            @NotBlank(message = "client_id is required")
            @JsonProperty("client_id")
            String clientId,

            @NotBlank(message = "client_secret is required")
            @JsonProperty("client_secret")
            String clientSecret,

            @NotBlank(message = "username is required")
            String username,

            @NotBlank(message = "password is required")
            String password
    ) {
    }

    /**
     * Response body for token endpoint.
     */
    public record TokenResponse(
            @JsonProperty("token_type")
            String tokenType,

            @JsonProperty("access_token")
            String accessToken,

            @JsonProperty("expires_in")
            long expiresIn
    ) {
    }

}

