package com.ss.as.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Global exception handler for the Authorization Server.
 * Provides consistent error responses following RFC 7807 Problem Details format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ProblemDetail handleOAuth2AuthenticationException(OAuth2AuthenticationException ex) {
        log.error("OAuth2 authentication error: {}", ex.getMessage(), ex);
        OAuth2Error error = ex.getError();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                error.getDescription() != null ? error.getDescription() : "OAuth2 authentication failed"
        );
        problem.setTitle(error.getErrorCode());
        problem.setType(URI.create(PROBLEM_BASE_URI));
        problem.setProperty("error", error.getErrorCode());
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
        );
        problem.setTitle("Authentication Error");
        problem.setType(URI.create(PROBLEM_BASE_URI));
        problem.setProperty("error", "invalid_grant");
        return problem;
    }

    @ExceptionHandler(InvalidClientException.class)
    public ProblemDetail handleInvalidClientException(InvalidClientException ex) {
        log.error("Invalid client error: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problem.setTitle("Invalid Client");
        problem.setType(URI.create(PROBLEM_BASE_URI));
        problem.setProperty("error", "invalid_client");
        return problem;
    }

}
