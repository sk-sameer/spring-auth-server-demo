package oauthserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * It provides a simple endpoint that returns a welcome message when the root URL ("/") is accessed.
 * This can be used to verify that the authorization server is running and accessible.
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "Welcome to the OAuth2 Authorization Server!";
    }
}
