package com.cfs.notification.security;

import java.util.List;
import java.math.BigDecimal;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT signing key is required");
        }
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public Claims extractClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(
                        signingKey
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(
            String token) {

        return extractClaims(token)
                .getSubject();
    }

    public List<String> extractRoles(
            String token) {
        return extractRoles(extractClaims(token));
    }

    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (!(roles instanceof List<?> values)) {
            throw new IllegalArgumentException("Invalid roles claim");
        }
        return values.stream().map(value -> {
            if (!(value instanceof String role) || !role.startsWith("ROLE_")
                    || role.length() <= 5 || role.chars().anyMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Invalid role");
            }
            return role;
        }).toList();
    }

    public Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Invalid userId claim");
        }
        try {
            long userId = new BigDecimal(value.toString()).longValueExact();
            if (userId <= 0) {
                throw new IllegalArgumentException("Invalid userId claim");
            }
            return userId;
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Invalid userId claim", exception);
        }
    }
}