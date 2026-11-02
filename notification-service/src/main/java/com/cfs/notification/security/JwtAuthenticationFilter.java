package com.cfs.notification.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader(
                        "Authorization"
                );

        if (authorization == null
                || !authorization.startsWith(
                "Bearer "
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorization.substring(7);

        try {

            Claims claims = jwtService.extractClaims(token);
            String username = claims.getSubject();
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Missing subject");
            }

            List<SimpleGrantedAuthority>
                    authorities =
                    jwtService
                            .extractRoles(claims)
                            .stream()
                            .map(
                                    SimpleGrantedAuthority::new
                            )
                            .toList();

            var authentication =
                    new NotificationJwtAuthenticationToken(
                            username,
                            jwtService.extractUserId(claims),
                            token,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (
                JwtException
                | IllegalArgumentException exception) {

            SecurityContextHolder
                    .clearContext();

            response.setStatus(
                    HttpServletResponse
                            .SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter()
                    .write("""
                    {
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid or expired JWT token"
                    }
                    """);

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}