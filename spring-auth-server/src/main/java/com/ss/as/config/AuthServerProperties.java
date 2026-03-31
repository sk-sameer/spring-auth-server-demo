package com.ss.as.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
        private List<String> grantTypes = List.of("authorization_code", "refresh_token");
        private boolean requireConsent = false;
    }

    @Getter
    @Setter
    public static class User {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
    }

}
