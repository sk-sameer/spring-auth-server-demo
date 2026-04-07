package com.ss.as.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

/**
 * Customizes JWT access tokens to include permissions based on user roles or client identity.
 */
@Component
@RequiredArgsConstructor
public class AuthorizationServerTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final AuthServerProperties properties;

    @Override
    public void customize(JwtEncodingContext context) {
        if (!isAccessTokenType(context.getTokenType())) return;

        AuthorizationGrantType authorizationGrantType = context.getAuthorizationGrantType();
        Authentication principal = context.getPrincipal();

        if (isAuthorizationCodeGrantType(authorizationGrantType) && principal.getPrincipal() instanceof User user) {
            context.getClaims().claim("permissions", getUserPermissions(user));
        } else if (isClientCredentialGrantType(authorizationGrantType) && principal instanceof OAuth2ClientAuthenticationToken clientAuth) {
            Map<String, List<String>> clientPermissions = properties.getPermissions().getClientPermissions();
            Optional.ofNullable(clientAuth.getRegisteredClient())
                    .ifPresent(client -> context.getClaims().claim("permissions", clientPermissions.get(client.getClientId())));
        }
    }

    private boolean isAccessTokenType(OAuth2TokenType tokenType) {
        return OAuth2TokenType.ACCESS_TOKEN.equals(tokenType);
    }

    private boolean isAuthorizationCodeGrantType(AuthorizationGrantType authorizationGrantType) {
        return AUTHORIZATION_CODE.equals(authorizationGrantType);
    }

    private Set<String> getUserPermissions(User user) {
        Map<String, List<String>> rolePermissions = properties.getPermissions().getRolePermissions();
        Set<String> userRoles = getUserRoles(user);
        return getPermissionsByUserRole(userRoles, rolePermissions);
    }

    private Set<String> getUserRoles(User user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> getPermissionsByUserRole(Set<String> userRoles, Map<String, List<String>> rolePermissions) {
        return userRoles.stream()
                .filter(rolePermissions::containsKey)
                .flatMap(role -> rolePermissions.get(role).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isClientCredentialGrantType(AuthorizationGrantType authorizationGrantType) {
        return CLIENT_CREDENTIALS.equals(authorizationGrantType);
    }

}

