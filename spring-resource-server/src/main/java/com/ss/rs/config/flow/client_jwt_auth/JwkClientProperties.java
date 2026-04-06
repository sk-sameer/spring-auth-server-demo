package com.ss.rs.config.flow.client_jwt_auth;

import com.nimbusds.jose.JWSAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized configuration properties for OAuth2 client JWK settings.
 * Supports both runtime RSA key generation and keystore-based key loading for private_key_jwt authentication.
 * And also supports symmetric key configuration for client_secret_jwt authentication.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.client")
public class JwkClientProperties {

    private String registrationId;
    private String authenticationMethod;
    private SymmetricKeyConfig symmetricKey = new SymmetricKeyConfig();
    private JwkConfig jwk = new JwkConfig();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymmetricKeyConfig {
        private String algorithm; //HmacSHA256, HmacSHA384, or HmacSHA512
        private JWSAlgorithm jwsAlgorithm; //HMAC signing: HS256, HS384, or HS512
    }

    /**
     * JWK and keystore configuration for private_key_jwt and key management.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class JwkConfig {
        /**
         * JWK source strategy: "runtime" generates RSA key pair at startup,
         * "keystore" loads from PKCS12 file specified in keystore.path property.
         */
        private String source = "runtime";

        /**
         * Keystore configuration for loading RSA keys from file.
         */
        private KeystoreConfig keystore = new KeystoreConfig();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeystoreConfig {
        /**
         * Path to the keystore file (supports classpath: prefix)
         * Example: classpath:client-keystore.p12 or file:./keystore.p12
         */
        private String path;
        private String password;
        private String alias;
        private String type;
    }

    public boolean isKeystoreSource() {
        return "keystore".equalsIgnoreCase(jwk.source);
    }

    public boolean isRuntimeSource() {
        return "runtime".equalsIgnoreCase(jwk.source) || jwk.source == null;
    }

}

