package com.ss.ac.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientService is responsible for making HTTP requests to protected resources using WebClient.
 * It retrieves the access token from the security context and includes it in the Authorization header of the request.
 */
@Service
public class WebClientService {

    private final WebClient webClient;
    private final OAuth2AuthorizedClientService authorizedClientService;


    public WebClientService(WebClient.Builder webClientBuilder, OAuth2AuthorizedClientService authorizedClientService) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:9000").build();
        this.authorizedClientService = authorizedClientService;
    }

    public String resource() {
        String accessToken = getAccessToken();
        return webClient.get()
                .uri("http://localhost:8083/read-resource")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

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
        throw new IllegalStateException("No access token available");
    }

}
