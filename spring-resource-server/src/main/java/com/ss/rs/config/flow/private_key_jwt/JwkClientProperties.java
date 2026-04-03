package com.ss.rs.config.flow.private_key_jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Externalized configuration properties for JWK client keys used in private_key_jwt authentication.
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.client.jwk")
public class JwkClientProperties {

    /**
     * JWK source: runtime or keystore
     * - runtime: Generate RSA key pair at runtime
     * - keystore: Load RSA key pair from keystore file
     */
    private String source = "runtime";

    private KeystoreConfig keystore = new KeystoreConfig();

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
        return "keystore".equalsIgnoreCase(source);
    }

    public boolean isRuntimeSource() {
        return "runtime".equalsIgnoreCase(source) || source == null;
    }

}

