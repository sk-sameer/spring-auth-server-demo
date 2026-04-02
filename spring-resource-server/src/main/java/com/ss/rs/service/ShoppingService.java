package com.ss.rs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal;

/**
 * Service for making authenticated HTTP requests to protected resource servers.
 * Uses WebClient with OAuth2 access tokens for authorization.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShoppingService {

    @Value("${app.external-services.shopping-service.base-url}")
    private String shoppingServiceBaseUrl;

    @Value("${app.external-services.shopping-service.get-products-url}")
    private String getProductsUrl;

    private final RestClient restClient;

    /**
     * Fetches a protected resource from the resource server.
     *
     * @return the resource response as a String
     * @throws IllegalStateException if no access token is available
     */
    public String getProducts() {
        log.debug("Making request to shopping service with access token");
        return restClient.get()
                .uri(shoppingServiceBaseUrl + getProductsUrl)
                .attributes(clientRegistrationId("shopping-service"))
                // Use a fixed principal name to scope the access token to the application, there will only be a single access token, and it will be used for all requests.
                .attributes(principal("my-application"))
                .retrieve()
                .body(String.class);
    }

}
