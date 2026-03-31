package com.ss.ac.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the OAuth2 Client application.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private ResourceServer resourceServer = new ResourceServer();

    @Getter
    @Setter
    public static class ResourceServer {
        private String baseUrl;
    }
}
