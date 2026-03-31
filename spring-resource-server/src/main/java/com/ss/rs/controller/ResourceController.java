package com.ss.rs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates protected resource endpoints with different authorization levels.
 *
 * <p><b>Authorization Levels:</b>
 * <ul>
 *   <li>/resource - Requires valid JWT token (any authenticated user)</li>
 *   <li>/read-resource - Requires 'user.read' scope in JWT token</li>
 *   <li>/write-resource - Requires 'user.write' scope (without SCOPE_ prefix)</li>
 * </ul>
 *
 * <p><b>Note:</b> Scopes are granted during authorization at the Authorization Server
 * and embedded in the JWT token. Spring Security automatically validates these scopes
 * when evaluating @PreAuthorize annotations. Use 'SCOPE_' prefix when checking OAuth2 scopes.</p>
 */
@RestController
public class ResourceController {

    @GetMapping("/resource")
    public String resource() {
        return "This is a protected resource";
    }

    @GetMapping("read-resource")
    @PreAuthorize("hasAuthority('SCOPE_user.read')")
    public String readResource() {
        return "This resource is available to users with 'user.read' authority.";
    }

    @GetMapping("write-resource")
    @PreAuthorize("hasAuthority('user.write')")
    public String writeResource() {
        return "This resource is available to users with 'user.write' authority.";
    }

}
