package com.company.asset.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-exp-ms}") long accessExpMs,
            @Value("${app.security.jwt.refresh-exp-ms}") long refreshExpMs
    ) {
        // secret 길이가 짧으면 예외 발생 가능 -> 운영은 환경변수로 길게
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String createAccessToken(Long userId, String email, String role) {
        return createToken(userId, email, role, accessExpMs);
    }

    public String createRefreshToken(Long userId, String email, String role) {
        return createToken(userId, email, role, refreshExpMs);
    }

    private String createToken(Long userId, String email, String role, long expMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .claims(Map.of(
                        "uid", userId,
                        "email", email,
                        "role", role
                ))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public Long getUserId(String token) {
        return parse(token).getPayload().get("uid", Long.class);
    }

    public String getEmail(String token) {
        return parse(token).getPayload().get("email", String.class);
    }

    public String getRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }
}
