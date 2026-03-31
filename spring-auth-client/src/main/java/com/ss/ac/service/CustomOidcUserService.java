package com.ss.ac.service;

import com.ss.ac.config.JwtDecoderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
 * By default, Spring Security only extracts standard OIDC claims (like sub, email, name) from the ID token.
 * However, Keycloak stores client-specific roles in a non-standard location within the access token.
 * This is not a standard OIDC claim, so Spring doesn't automatically map these roles to GrantedAuthority.
 * CustomOidcUserService manually extracts these client-specific roles from the access token's resource_access claim
 * and converts them into Spring Security authorities (ROLE_USER, ROLE_ADMIN, etc.).
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final JwtDecoderFactory jwtDecoderFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser defaultUser = super.loadUser(userRequest);

        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        System.out.println("Access Token: " + accessToken.getTokenValue());
        //Extract the role
        Set<GrantedAuthority> roles = extractRolesFromAccessToken(userRequest);

        //Combine the default authorities with the extracted roles
        roles.addAll(defaultUser.getAuthorities());

        return new DefaultOidcUser(roles, defaultUser.getIdToken(), defaultUser.getUserInfo());
    }

    // Extracts Keycloak IAM roles from the access token's resource_access claim and converts them to Spring Security authorities.
    private Set<GrantedAuthority> extractRolesFromAccessToken(OidcUserRequest userRequest) {

        JwtDecoder jwtDecoder = jwtDecoderFactory.getJwtDecoder(userRequest.getClientRegistration().getRegistrationId());
        Jwt jwt = jwtDecoder.decode(userRequest.getAccessToken().getTokenValue());

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (CollectionUtils.isEmpty(resourceAccess)) {
            return new HashSet<>();
        }

        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get(userRequest.getClientRegistration().getClientId());
        if (CollectionUtils.isEmpty(clientResource)) {
            return new HashSet<>();
        }

        List<String> roles = (List<String>) clientResource.get("roles");
        if (CollectionUtils.isEmpty(roles)) {
            return new HashSet<>();
        }

        Set<GrantedAuthority> simpleGrantedAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());

        return simpleGrantedAuthorities;
    }

}
