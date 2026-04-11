package com.ss.rs.sh.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwtToken) {

        Set<GrantedAuthority> permissions = Optional.ofNullable(jwtToken.getClaimAsStringList("permissions"))
                .stream()
                .flatMap(Collection::stream)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());

        return new JwtAuthenticationToken(jwtToken, permissions);
    }

}
