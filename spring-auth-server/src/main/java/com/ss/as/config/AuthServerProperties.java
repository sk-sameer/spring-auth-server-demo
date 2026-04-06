package com.ss.as.config;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Externalized configuration properties for the Authorization Server.
 * All sensitive and environment-specific settings should be configured via application.yml.
 */
@Component
@ConfigurationProperties(prefix = "auth-server")
@Validated
@Getter
@Setter
public class AuthServerProperties {

    private String issuerUri;
    private List<Client> clients = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @Getter
    @Setter
    public static class Client {
        private String clientId;
        private String clientSecret;
        private List<String> redirectUris = new ArrayList<>();
        private List<String> scopes = new ArrayList<>();
        private List<AuthorizationGrantType> grantTypes = new ArrayList<>();
        private List<ClientAuthenticationMethod> clientAuthenticationMethods = new ArrayList<>();
        private boolean requireConsent = false;

        @Valid
        private JwkConfig jwks; // Optional configuration for JWKS if using private_key_jwt authentication method

        public void setGrantTypes(List<String> grantTypes) {
            this.grantTypes = grantTypes.stream()
                    .map(this::mapGrantType)
                    .toList();
        }

        public void setClientAuthenticationMethods(List<String> method) {
            this.clientAuthenticationMethods = method.stream()
                    .map(ClientAuthenticationMethod::valueOf)
                    .toList();
        }

        public boolean isJwksUriConfigured() {
            return Optional.ofNullable(jwks)
                    .filter(j -> StringUtils.hasText(j.uri))
                    .isPresent();
        }

        public boolean isJwksSigningAlgorithmConfigured() {
            return Optional.ofNullable(jwks)
                    .filter(j -> j.signingAlgorithm != null)
                    .isPresent();
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

    @Getter
    @Setter
    public static class User {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class JwkConfig {
        private String uri;
        private JwsAlgorithm signingAlgorithm;

        public void setSigningAlgorithm(String signingAlgorithm) {
            if (signingAlgorithm == null) {
                throw new IllegalArgumentException("Signing algorithm cannot be null");
            }

            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.from(signingAlgorithm);
            if (signatureAlgorithm != null) {
                this.signingAlgorithm = signatureAlgorithm;
                return;
            }

            MacAlgorithm macAlgorithm = MacAlgorithm.from(signingAlgorithm);
            if (macAlgorithm != null) {
                this.signingAlgorithm = macAlgorithm;
                return;
            }

            throw new IllegalArgumentException("Unsupported signing algorithm: " + signingAlgorithm);
        }

    }

}
