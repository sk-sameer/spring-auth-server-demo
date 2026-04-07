package com.ss.as.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.List;
import java.util.UUID;

/**
 * Configuration for OAuth2 registered clients.
 * In production, consider using JdbcRegisteredClientRepository for persistent storage.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final AuthServerProperties properties;

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
        List<AuthServerProperties.Client> configuredClients = properties.getClients();

        if (configuredClients.isEmpty()) {
            // Fallback default client for development
            RegisteredClient defaultClient = buildDefaultClient(encoder);
            return new InMemoryRegisteredClientRepository(defaultClient);
        }

        List<RegisteredClient> clients = configuredClients.stream()
                .map(c -> buildRegisteredClient(c, encoder))
                .toList();

        return new InMemoryRegisteredClientRepository(clients);
    }

    private RegisteredClient buildRegisteredClient(AuthServerProperties.Client clientConfig, PasswordEncoder encoder) {
        // Determine how to store the client secret based on authentication method
        String clientSecret = encodeClientSecret(clientConfig, encoder);

        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientConfig.getClientId())
                .clientSecret(clientSecret)
                .scopes(scopes -> scopes.addAll(clientConfig.getScopes()))
                .redirectUris(uris -> uris.addAll(clientConfig.getRedirectUris()))
                .clientAuthenticationMethods(authMethods ->
                        authMethods.addAll(clientConfig.getClientAuthenticationMethods()))
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(clientConfig.getGrantTypes()))
                .clientSettings(buildClientSettings(clientConfig))
                .build();
    }

    /**
     * Encodes client secret based on the authentication method.
     *
     * <p><b>Storage Strategy:</b></p>
     * <ul>
     *   <li><b>client_secret_jwt:</b> Returns raw secret (no encoding).
     *       Spring Authorization Server's JwtClientAssertionDecoderFactory retrieves this value
     *       directly and uses it as the HMAC key. HMAC verification requires the original secret,
     *       not a hash, so encoding would break MAC verification.</li>
     *   <li><b>client_secret_basic, client_secret_post:</b> Uses PasswordEncoder.encode() (BCrypt).
     *       Passwords are hashed for secure storage and compared using the encoder during
     *       client authentication.</li>
     * </ul>
     *
     * @param clientConfig the client configuration containing the raw secret
     * @param encoder      the password encoder for hashing (BCrypt)
     * @return properly formatted secret for the authentication method
     */
    private String encodeClientSecret(AuthServerProperties.Client clientConfig, PasswordEncoder encoder) {
        String rawSecret = clientConfig.getClientSecret();

        if (isClientSecretJwtAuthenticationMethodEnabled(clientConfig)) {
            log.debug("Client '{}' uses client_secret_jwt - storing raw secret.", clientConfig.getClientId());
            return rawSecret;
        }

        log.debug("Client '{}' uses password based authentication - encoding secret.", clientConfig.getClientId());
        return encoder.encode(rawSecret);
    }

    private boolean isClientSecretJwtAuthenticationMethodEnabled(AuthServerProperties.Client clientConfig) {
        return clientConfig.getClientAuthenticationMethods()
                .contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
    }

    private ClientSettings buildClientSettings(AuthServerProperties.Client clientConfig) {
        ClientSettings.Builder builder = ClientSettings.builder()
                .requireAuthorizationConsent(clientConfig.isRequireConsent());

        if (clientConfig.isJwksUriConfigured()) {
            builder.jwkSetUrl(clientConfig.getJwks().getUri());
        }

        if (clientConfig.isJwksSigningAlgorithmConfigured()) {
            builder.tokenEndpointAuthenticationSigningAlgorithm(clientConfig.getJwks().getSigningAlgorithm());
        }

        return builder.build();
    }

    private RegisteredClient buildDefaultClient(PasswordEncoder encoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("myclient")
                .clientSecret(encoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login/oauth2/code/myclient")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();
    }

}
