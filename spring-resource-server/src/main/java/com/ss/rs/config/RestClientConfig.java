package com.ss.rs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.web.client.RestClient;

/**
 * Configures RestClient bean with OAuth2 interceptor for service-to-service authentication.
 * The interceptor uses OAuth2AuthorizedClientManager to automatically inject Bearer tokens
 * out outbound requests using the application identity instead of per-user credentials.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates RestClient bean with OAuth2 interceptor for token-based authentication.
     * Uses RequestAttributePrincipalResolver to scope tokens to application identity.
     * @param authorizedClientManager Manager for retrieving and caching access tokens
     * @return RestClient configured with OAuth2 interceptor
     */
    @Bean
    public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        // By default, access tokens are scoped to the principal name of the current user which means every user will receive a unique access token.
        // to scope access tokens to the application, you will need to set a strategy for resolving a custom principal (application itself) name.
        requestInterceptor.setPrincipalResolver(new RequestAttributePrincipalResolver());

        return RestClient.builder()
                .requestInterceptor(requestInterceptor)
                .build();
    }

}
