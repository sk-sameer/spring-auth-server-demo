package com.ss.rs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.web.client.RestClient;

/**
 * This configuration class defines a RestClient bean that is configured to use OAuth2 for authentication.
 * The RestClient is set up with an OAuth2ClientHttpRequestInterceptor that uses an OAuth2AuthorizedClientManager to manage the authorized clients.
 * The interceptor is also configured with a RequestAttributePrincipalResolver to resolve the principal for the access token.
 */
@Configuration
public class RestClientConfig {

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
