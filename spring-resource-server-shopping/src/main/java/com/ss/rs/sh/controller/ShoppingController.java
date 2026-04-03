package com.ss.rs.sh.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates protected resource endpoint.
 *
 * <p><b>Authorization Levels:</b>
 * <ul>
 *   <li>/read-resource - Requires 'user.read' scope in JWT token</li>
 * </ul>
 *
 * <p><b>Note:</b> OAuth2 scopes are automatically prefixed with 'SCOPE_' by Spring Security.
 * Use 'SCOPE_' prefix when checking OAuth2 scopes in @PreAuthorize annotations.</p>
 */
@RestController
@RequestMapping("/api")
public class ShoppingController {

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('SCOPE_user.read')")
    public String getProducts() {
        return "Returning list of available products: Product A, Product B, Product C";
    }

}
