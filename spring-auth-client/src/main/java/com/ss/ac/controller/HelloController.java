package com.ss.ac.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * It provides an endpoint to greet the authenticated user.
 * It uses the Principal object to retrieve the user's name and returns a personalized greeting message.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello " + principal.getName() + ", Welcome to the Spring Security Oauth2 Client Application";
    }

}
