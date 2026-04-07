package com.ss.rs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates protected resource endpoints with different authorization levels.
 *
 * <p><b>Authorization Levels:</b>
 * <ul>
 *   <li>/resource - Requires valid JWT token (any authenticated user)</li>
 *   <li>/read-resource - Requires 'user.read' scope in JWT token</li>
 *   <li>/write-resource - Requires 'user.write' scope in JWT token</li>
 * </ul>
 *
 * <p><b>Note:</b> OAuth2 scopes are automatically prefixed with 'SCOPE_' by Spring Security.
 * Use 'SCOPE_' prefix when checking OAuth2 scopes in @PreAuthorize annotations.</p>
 */
@RestController
public class ResourceController {

    @GetMapping("/resource")
    public String resource(@AuthenticationPrincipal Jwt jwt) {
        String subject = jwt != null ? jwt.getSubject() : "anonymous";
        return "This is a protected resource. Hello, " + subject + "!";
    }

    @GetMapping("/read-resource")
    @PreAuthorize("hasAuthority('read')")
    public String readResource(@AuthenticationPrincipal Jwt jwt) {
        return "Read access granted for user: " + jwt.getSubject();
    }

    @GetMapping("/write-resource")
    @PreAuthorize("hasAuthority('write')")
    public String writeResource(@AuthenticationPrincipal Jwt jwt) {
        return "Write access granted for user: " + jwt.getSubject();
    }
}
