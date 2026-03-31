package com.ss.ac.service;

import com.ss.ac.config.JwtDecoderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom OIDC user service that extracts additional authorities from the access token.
 *
 * <p>By default, Spring Security only extracts standard OIDC claims (like sub, email, name) from the ID token.
 * This service extracts additional roles/authorities from the access token, supporting both:</p>
 * <ul>
 *   <li>Keycloak's resource_access claim structure</li>
 *   <li>Custom 'authorities' claim (from Spring Authorization Server)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final JwtDecoderFactory jwtDecoderFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser defaultUser = super.loadUser(userRequest);

        log.debug("Processing access token for user: {}", defaultUser.getName());

        // Extract roles from access token
        Set<GrantedAuthority> roles = extractRolesFromAccessToken(userRequest);

        //Combine the default authorities with the extracted roles
        roles.addAll(defaultUser.getAuthorities());

        return new DefaultOidcUser(roles, defaultUser.getIdToken(), defaultUser.getUserInfo());
    }

    /**
     * Extracts roles from the access token.
     * Supports Keycloak's resource_access claim and custom authorities claim.
     */
    private Set<GrantedAuthority> extractRolesFromAccessToken(OidcUserRequest userRequest) {
        try {
            JwtDecoder jwtDecoder = jwtDecoderFactory.getJwtDecoder(userRequest.getClientRegistration().getRegistrationId());
            Jwt jwt = jwtDecoder.decode(userRequest.getAccessToken().getTokenValue());

            Set<GrantedAuthority> authorities = new HashSet<>();

            // Try Keycloak resource_access structure
            authorities.addAll(extractKeycloakRoles(jwt, userRequest.getClientRegistration().getClientId()));

            // Try custom authorities claim (Spring Authorization Server)
            authorities.addAll(extractAuthoritiesClaim(jwt));

            return authorities;
        } catch (Exception e) {
            log.warn("Failed to extract roles from access token: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Extracts Keycloak roles from resource_access claim.
     */
    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractKeycloakRoles(Jwt jwt, String clientId) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (CollectionUtils.isEmpty(resourceAccess)) {
            return new HashSet<>();
        }

        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(clientId);
        if (CollectionUtils.isEmpty(clientResource)) {
            return new HashSet<>();
        }

        List<String> roles = (List<String>) clientResource.get("roles");
        if (CollectionUtils.isEmpty(roles)) {
            return new HashSet<>();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    /**
     * Extracts authorities from custom 'authorities' claim.
     */
    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractAuthoritiesClaim(Jwt jwt) {
        Object authoritiesClaim = jwt.getClaim("authorities");
        if (authoritiesClaim == null) {
            return new HashSet<>();
        }

        if (authoritiesClaim instanceof List<?> authorities) {
            return ((List<String>) authorities).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        return new HashSet<>();
    }

}
