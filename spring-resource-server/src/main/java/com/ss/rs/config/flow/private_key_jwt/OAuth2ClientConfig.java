package com.ss.rs.config.flow.private_key_jwt;

import com.nimbusds.jose.jwk.JWK;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.function.Function;

/**
 * Configuration for OAuth2 Client with support for private_key_jwt authentication.
 * This enables the resource server to act as an OAuth2 client for service-to-service calls.
 */
@Configuration
@RequiredArgsConstructor
public class OAuth2ClientConfig {

    private final Function<ClientRegistration, JWK> clientJwkResolver;

    /**
     * Creates an OAuth2AuthorizedClientManager for managing authorized clients.
     * Configured with private_key_jwt support for client_credentials grant type.
     *
     * @param clientRegistrationRepository Repository of client registrations
     * @param authorizedClientService      Service for managing authorized clients
     * @return Configured OAuth2AuthorizedClientManager
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        // Create token response client with private_key_jwt support
        RestClientClientCredentialsTokenResponseClient tokenResponseClient = new RestClientClientCredentialsTokenResponseClient();

        // Add JWT authentication parameters converter
        tokenResponseClient.addParametersConverter(new NimbusJwtClientAuthenticationParametersConverter<>(clientJwkResolver));

        // Create client credentials provider with custom token response client
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(configurer -> configurer.accessTokenResponseClient(tokenResponseClient))
                        .build();

        // Create and configure the authorized client manager
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

}
