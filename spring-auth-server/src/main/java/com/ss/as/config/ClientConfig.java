package com.ss.as.config;

import lombok.RequiredArgsConstructor;
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
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientConfig.getClientId())
                .clientSecret(encoder.encode(clientConfig.getClientSecret()))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

        for (String grantType : clientConfig.getGrantTypes()) {
            builder.authorizationGrantType(mapGrantType(grantType));
        }

        for (String redirectUri : clientConfig.getRedirectUris()) {
            builder.redirectUri(redirectUri);
        }

        for (String scope : clientConfig.getScopes()) {
            builder.scope(scope);
        }

        builder.clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(clientConfig.isRequireConsent())
                .build());

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
                .scope("user.read")
                .scope("user.write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();
    }

    private AuthorizationGrantType mapGrantType(String grantType) {
        return switch (grantType.toLowerCase()) {
            case "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE;
            case "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN;
            case "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS;
            case "jwt_bearer" -> AuthorizationGrantType.JWT_BEARER;
            case "device_code" -> AuthorizationGrantType.DEVICE_CODE;
            case "token_exchange" -> AuthorizationGrantType.TOKEN_EXCHANGE;
            default -> new AuthorizationGrantType(grantType);
        };
    }
}
