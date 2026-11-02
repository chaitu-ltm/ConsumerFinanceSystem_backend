package com.cfs.disbursement.config;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.jwt.secret-base64}")
    private String jwtSecretBase64;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] decodedSecret =
                Base64.getDecoder()
                        .decode(jwtSecretBase64);

        SecretKey secretKey =
                new SecretKeySpec(
                        decodedSecret,
                        "HmacSHA256"
                );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        return jwt -> {

            List<String> roles = extractRoles(jwt);

            Collection<GrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            String username = jwt.getSubject();

            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    username
            );
        };
    }

    private List<String> extractRoles(Jwt jwt) {

        Object rolesClaim =
                jwt.getClaim("roles");

        if (rolesClaim instanceof Collection<?> roles) {

            return roles.stream()
                    .map(Object::toString)
                    .toList();
        }

        if (rolesClaim instanceof String role) {

            return List.of(role);
        }

        Object authoritiesClaim =
                jwt.getClaim("authorities");

        if (authoritiesClaim
                instanceof Collection<?> authorities) {

            return authorities.stream()
                    .map(Object::toString)
                    .toList();
        }

        Object roleClaim =
                jwt.getClaim("role");

        if (roleClaim instanceof String role) {

            return List.of(role);
        }

        return List.of();
    }
}