package com.cfs.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cfs.notification.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter) {

        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                .csrf(csrf ->
                        csrf.disable()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                        }))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Health and Swagger
                         */
                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        /*
                         * Manual admin creation; producer integration is not implemented.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/notifications/internal"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Admin can retrieve all notifications.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Customer/admin notification APIs.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications/customer/**"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN"
                        )
                        .requestMatchers(HttpMethod.GET, "/api/notifications/*")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        /*
                         * Mark notification as read.
                         */
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/notifications/*/read"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN"
                        )

                        /*
                         * Delete notification.
                         */
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/notifications/**"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "ADMIN"
                        )

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}