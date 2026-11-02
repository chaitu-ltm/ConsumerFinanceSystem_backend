package com.cfs.customerservice.config;

import java.util.Base64;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${security.jwt.secret-base64}")
	private String jwtSecretBase64;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers("/api/admin/kyc", "/api/admin/kyc/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/customers/*/kyc-status").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/customers/*/kyc-review").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/customers").hasRole("CUSTOMER")
						.requestMatchers("/api/customers/**").hasRole("CUSTOMER")
						.anyRequest().denyAll())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
						.accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
						.decoder(jwtDecoder())
						.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {

		byte[] decodedSecret = Base64.getDecoder().decode(jwtSecretBase64);
		SecretKey secretKey = new SecretKeySpec(decodedSecret, "HmacSHA256");

		return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {

		return jwt -> {

			Object claim = jwt.getClaim("roles");
			List<String> roles;

			if (claim instanceof Collection<?> values) {
				roles = values.stream().map(Object::toString).toList();
			} else {
				roles = List.of();
			}

			List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
					.map(GrantedAuthority.class::cast).toList();

			return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
		};
	}
}
