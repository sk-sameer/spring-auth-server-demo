package com.ss.rs.config.flow.client_jwt_auth.client_secret_jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.ss.rs.config.flow.client_jwt_auth.JwkClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Configuration that provides a JWK resolver for client_secret_jwt (symmetric key) authentication.
 * Caches derived JWKs from client secrets to improve performance during OAuth2 client authentication.
 * Enables resource servers to authenticate as OAuth2 clients using symmetric cryptography.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.authentication-method", havingValue = "client_secret_jwt")
public class ClientJwtKeyConfig {

    private final JwkClientProperties jwkClientProperties;
    private final Map<String, JWK> keyCache = new ConcurrentHashMap<>();

    /**
     * Creates a JWK resolver function that derives symmetric keys from client secrets
     * for OAuth2 client authentication using the client_secret_jwt method.
     * @return Function that resolves cached OctetSequenceKey (HMAC) for client registrations
     */
    @Bean
    public Function<ClientRegistration, JWK> clientJwkResolver() {
        return clientRegistration -> {
            // For client_secret_jwt, we can return a symmetric key based on the client secret
            if (clientRegistration.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.CLIENT_SECRET_JWT)) {
                return keyCache.computeIfAbsent(clientRegistration.getRegistrationId(), clientId -> {

                            SecretKeySpec secretKey = new SecretKeySpec(
                                    clientRegistration.getClientSecret().getBytes(StandardCharsets.UTF_8),
                                    jwkClientProperties.getSymmetricKey().getAlgorithm());

                            return new OctetSequenceKey.Builder(secretKey)
                                    .keyID(clientId)
                                    .algorithm(jwkClientProperties.getSymmetricKey().getJwsAlgorithm())
                                    .build();
                        }
                );
            }
            return null;
        };
    }

}
