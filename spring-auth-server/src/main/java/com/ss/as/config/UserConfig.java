package com.ss.as.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

/**
 * Configuration for user authentication.
 * In production, replace InMemoryUserDetailsManager with a database-backed implementation.
 */
@Configuration
@RequiredArgsConstructor
public class UserConfig {

    private final AuthServerProperties properties;

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        List<AuthServerProperties.User> configuredUsers = properties.getUsers();

        if (configuredUsers.isEmpty()) {
            // Fallback default user for development
            UserDetails defaultUser = User.builder()
                    .username("user")
                    .password(encoder.encode("password"))
                    .roles("USER")
                    .build();
            return new InMemoryUserDetailsManager(defaultUser);
        }

        List<UserDetails> users = configuredUsers.stream()
                .map(u -> User.builder()
                        .username(u.getUsername())
                        .password(encoder.encode(u.getPassword()))
                        .roles(u.getRoles().toArray(new String[0]))
                        .build())
                .toList();

        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

}

