package com.cfs.auth.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import com.cfs.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;

	private final long expirationMs;

	public JwtService(@Value("${security.jwt.secret-base64}") String secretBase64,

			@Value("${security.jwt.expiration-ms}") long expirationMs) {

		byte[] secretBytes = Base64.getDecoder().decode(secretBase64);

		this.secretKey = Keys.hmacShaKeyFor(secretBytes);

		this.expirationMs = expirationMs;
	}

	public String generateToken(User user) {

		Date currentTime = new Date();

		Date expirationTime = new Date(currentTime.getTime() + expirationMs);

		return Jwts.builder().subject(user.getEmail()).claim("userId", user.getId())
				.claim("roles", user.getRoles().stream().map(role -> role.getName()).toList()).issuedAt(currentTime)
				.expiration(expirationTime).signWith(secretKey).compact();
	}

	public String extractEmail(String token) {

		return extractClaims(token).getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) {

		try {

			String email = extractEmail(token);

			Date expiration = extractClaims(token).getExpiration();

			return email.equalsIgnoreCase(userDetails.getUsername()) && expiration.after(new Date());

		} catch (JwtException | IllegalArgumentException exception) {

			return false;
		}
	}

	private Claims extractClaims(String token) {

		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}

	public long getExpirationMs() {
		return expirationMs;
	}
}