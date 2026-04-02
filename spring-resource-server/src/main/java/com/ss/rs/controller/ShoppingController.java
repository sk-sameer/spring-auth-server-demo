package com.ss.rs.controller;

import com.ss.rs.service.ShoppingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('SCOPE_user.read')")
    public String getProducts() {
        log.debug("Received request for /products endpoint");
        return shoppingService.getProducts();
    }

}
