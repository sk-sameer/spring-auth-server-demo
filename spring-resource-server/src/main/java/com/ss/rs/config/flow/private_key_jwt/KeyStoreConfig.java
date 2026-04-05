package com.ss.rs.config.flow.private_key_jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.ss.rs.exception.KeystoreLoadException;
import com.ss.rs.exception.RsaKeyExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

/**
 * Configuration for loading RSA keys from a PKCS12 keystore for private_key_jwt authentication.
 * This class loads the keystore, extracts the RSA key pair, and provides it as a JWK for OAuth2 client authentication.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.jwk.source", havingValue = "keystore")
@ConditionalOnProperty(
        prefix = "spring.security.oauth2.client.registration.internal-resource-server-id",
        name = "client-authentication-method",
        havingValue = "private_key_jwt"
)
public class KeyStoreConfig {

    private final ResourceLoader resourceLoader;
    private final JwkClientProperties jwkClientProperties;

    @Bean
    public KeyStore keyStore() throws Exception {
        JwkClientProperties.KeystoreConfig keystoreConfig = jwkClientProperties.getKeystore();
        log.info("Loading keystore from: {}", keystoreConfig.getPath());

        KeyStore keyStore = KeyStore.getInstance(keystoreConfig.getType());
        Resource resource = resourceLoader.getResource(keystoreConfig.getPath());
        try (InputStream is = resource.getInputStream()) {
            keyStore.load(is, keystoreConfig.getPassword().toCharArray());
            log.info("Successfully loaded keystore");
        } catch (Exception e) {
            log.error("Failed to load keystore from path: {}. Make sure the file exists and is a valid keystore.", keystoreConfig.getPath(), e);
            throw new KeystoreLoadException("Keystore file not found or invalid: " + keystoreConfig.getPath(), e);
        }
        return keyStore;
    }

    @Bean
    public RSAKey rsaKey(KeyStore keyStore) throws Exception {
        JwkClientProperties.KeystoreConfig keystoreConfig = jwkClientProperties.getKeystore();
        String keystoreAlias = keystoreConfig.getAlias();
        log.info("Extracting RSA key from keystore with alias: {}", keystoreAlias);

        try {
            // Extract private key
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, keystoreConfig.getPassword().toCharArray());

            if (privateKey == null) {
                throw new RuntimeException("Private key not found with alias: " + keystoreAlias);
            }

            // Extract public key from the certificate
            Certificate cert = keyStore.getCertificate(keystoreAlias);
            if (cert == null) {
                throw new RuntimeException("Certificate not found with alias: " + keystoreAlias);
            }

            RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

            log.info("Successfully extracted RSA key pair from keystore");

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keystoreAlias)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyUse(KeyUse.SIGNATURE)
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract RSA key from keystore", e);
            throw new RsaKeyExtractionException("Failed to extract RSA key from keystore: " + e.getMessage(), e);
        }
    }

}
