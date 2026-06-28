package com.vitaclinica.agendamento.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String gerarToken(Map<String, Object> claims, String cpf) {
        return Jwts.builder()
                .claims(claims)
                .subject(cpf)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extrairCpf(String token) {
        return extrairClaims(token).getSubject();
    }

    public String extrairRole(String token) {
        return (String) extrairClaims(token).get("role");
    }

    public Long extrairUserId(String token) {
        Object id = extrairClaims(token).get("userId");
        return id instanceof Integer ? ((Integer) id).longValue() : (Long) id;
    }

    public boolean isTokenValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
