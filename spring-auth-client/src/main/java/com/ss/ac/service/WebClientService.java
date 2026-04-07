package com.ss.ac.service;

import com.ss.ac.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service for making authenticated HTTP requests to protected resource servers.
 * Uses WebClient with OAuth2 access tokens for authorization.
 */
@Service
@Slf4j
public class WebClientService {

    private final WebClient webClient;
    private final AppProperties appProperties;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public WebClientService(WebClient.Builder webClientBuilder,
                            OAuth2AuthorizedClientService authorizedClientService,
                            AppProperties appProperties) {

        this.appProperties = appProperties;
        this.authorizedClientService = authorizedClientService;
        this.webClient = webClientBuilder
                .baseUrl(appProperties.getResourceServer().getBaseUrl())
                .build();
        log.info("WebClientService initialized with resource server: {}", appProperties.getResourceServer().getBaseUrl());
    }

    /**
     * Fetches a protected resource from the resource server.
     *
     * @return the resource response as a String
     * @throws IllegalStateException if no access token is available
     */
    public String resource() {
        String accessToken = getAccessToken();
        log.debug("Making request to resource server with access token");

        return webClient.get()
                .uri(appProperties.getResourceServer().getGetEndpoint())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Note: Consider using reactive approach in production
    }

    /**
     * Retrieves the access token from the current security context.
     *
     * @return the access token value
     * @throws IllegalStateException if no valid OAuth2 authentication or token is available
     */
    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null && client.getAccessToken() != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        throw new IllegalStateException("No access token available. User may not be authenticated.");
    }

}
