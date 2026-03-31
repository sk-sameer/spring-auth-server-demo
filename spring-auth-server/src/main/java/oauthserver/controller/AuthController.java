package oauthserver.controller;

import lombok.RequiredArgsConstructor;
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
import java.util.Map;

/**
 * Provides custom authentication endpoint for JWT token generation.
 *
 * <p><b>Purpose:</b> This endpoint enables direct username/password authentication with immediate
 * JWT token issuance, bypassing the standard OAuth2 authorization code flow. Useful for
 * Testing and development scenarios requiring quick token generation
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    /**
     * Authenticates a user and issues a JWT access token.
     *
     * <p>This endpoint requires both client credentials (client_id, client_secret) and
     * user credentials (username, password) to be provided in the request body.</p>
     *
     * @param request Map containing:
     *                - client_id: The OAuth2 client identifier
     *                - client_secret: The OAuth2 client secret (plain text, will be matched against encoded secret)
     *                - username: The user's username
     *                - password: The user's password (plain text, will be authenticated)
     * @return Map containing:
     * - access_token: The generated JWT token
     * - token_type: "Bearer"
     * - expires_in: Token expiration time in seconds (3600 = 1 hour)
     * @throws RuntimeException                                          if client credentials are invalid
     * @throws org.springframework.security.core.AuthenticationException if user credentials are invalid
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        RegisteredClient client = registeredClientRepository.findByClientId(request.get("client_id").toString());
        if (client == null || !passwordEncoder.matches(request.get("client_secret").toString(), client.getClientSecret())) {
            throw new RuntimeException("Invalid client credentials");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("username").toString(), request.get("password").toString())
        );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:9000")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(authentication.getName())
                .claim("scope", "api.read")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return Map.of("access_token", token, "token_type", "Bearer", "expires_in", 3600);
    }

}
