package com.ss.ac.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and caching JwtDecoder instances based on client registration IDs.
 * This allows for efficient retrieval of JwtDecoders for multiple OAuth2 providers.
 * A JwtDecoder is needed to validate and decode JWT tokens issued by OAuth2 providers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtDecoderFactory {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final Map<String, JwtDecoder> jwtDecoderCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a JwtDecoder for the given client registration ID. If a JwtDecoder for the specified
     * registration ID does not exist in the cache, it creates a new one using the issuer URI from the
     * client registration and stores it in the cache for future use.
     *
     * @param registrationId the client registration ID for which to retrieve the JwtDecoder
     * @return a JwtDecoder instance associated with the specified client registration ID
     * @throws IllegalArgumentException if no client registration is found for the given registration ID
     *                                  or if the issuer URI is not configured for that registration
     */
    public JwtDecoder getJwtDecoder(String registrationId) {
        return jwtDecoderCache.computeIfAbsent(registrationId, id -> {
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(id);

            if (clientRegistration == null) {
                throw new IllegalArgumentException("No client registration found for registrationId: " + id);
            }

            String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();

            if (issuerUri == null || issuerUri.isEmpty()) {
                throw new IllegalArgumentException("No issuer-uri configured for registrationId: " + id);
            }

            log.info("Creating JwtDecoder for issuerUri: {}", issuerUri);
            return JwtDecoders.fromIssuerLocation(issuerUri);
        });
    }

}
