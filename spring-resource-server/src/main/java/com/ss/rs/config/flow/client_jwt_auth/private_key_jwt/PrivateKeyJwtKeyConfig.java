package com.ss.rs.config.flow.client_jwt_auth.private_key_jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.ss.rs.config.flow.client_jwt_auth.JwkClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.function.Function;

/**
 * Configuration for client-side JWT signing keys for private_key_jwt authentication.
 * Generates an RSA key pair that will be used to sign JWT assertions when authenticating
 * with the Authorization Server using the private_key_jwt method.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.authentication-method", havingValue = "private_key_jwt")
public class PrivateKeyJwtKeyConfig {

    private RSAKey cacheRsaKey;

    private final JwkClientProperties jwkClientProperties;
    private final ObjectProvider<RSAKey> rsaKey; // Injected RSAKey bean for keystore loading (if configured)

    /**
     * Creates a JWK resolver function that provides RSA key pairs for OAuth2 client registrations
     * using private_key_jwt authentication.
     *
     * @return Function that resolves JWK for a given ClientRegistration
     */
    @Bean
    public Function<ClientRegistration, JWK> clientJwkResolver() {
        if (cacheRsaKey == null) {
            log.info("Creating RSA key pair for private_key_jwt client authentication");
            cacheRsaKey = getRSAKey();
        }

        return clientRegistration -> {
            if (clientRegistration.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.PRIVATE_KEY_JWT)) {
                return cacheRsaKey;
            }
            return null;
        };
    }

    private RSAKey getRSAKey() {
        if (jwkClientProperties.isKeystoreSource()) {
            log.info("Loading RSA key pair from keystore for private_key_jwt client authentication");
            return rsaKey.getIfAvailable();
        } else {
            log.info("Generating new RSA key pair at runtime for private_key_jwt client authentication");
            return generateRsaKey();
        }
    }

    /**
     * Generates an RSA key pair for JWT signing.
     *
     * @return RSAKey containing public and private keys
     */
    private static RSAKey generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            log.info("Generated RSA key pair for private_key_jwt client authentication");

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .algorithm(JWSAlgorithm.RS256)
                    .keyUse(KeyUse.SIGNATURE)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair for client", e);
        }
    }

}

