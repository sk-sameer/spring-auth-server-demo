package com.ss.as.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home controller providing a simple health check endpoint.
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "Welcome to the OAuth2 Authorization Server!";
    }
}

