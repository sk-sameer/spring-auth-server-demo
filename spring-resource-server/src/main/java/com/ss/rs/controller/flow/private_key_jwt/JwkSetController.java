package com.ss.rs.controller.flow.private_key_jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.function.Function;

/**
 * Exposes the client's public keys as a JWK Set for the authorization server
 * to verify JWT signatures in private_key_jwt authentication.
 */
@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final Function<ClientRegistration, JWK> clientJwkResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/private_key_jwt/jwks")
    public Map<String, Object> jwkSet() {
        ClientRegistration clientRegistration =
                clientRegistrationRepository.findByRegistrationId("internal-resource-server-id");

        JWK jwk = clientJwkResolver.apply(clientRegistration);

        // Return only public key (not private key!)
        JWK publicJwk = jwk.toPublicJWK();

        JWKSet jwkSet = new JWKSet(publicJwk);

        return jwkSet.toJSONObject();
    }
}

