package com.ss.as.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

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

}
