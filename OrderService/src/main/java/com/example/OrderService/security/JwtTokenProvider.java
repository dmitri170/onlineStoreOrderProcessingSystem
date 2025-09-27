package com.example.OrderService.security;

import com.example.OrderService.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long validityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        if (secret.length() < 32) {
            log.warn("JWT secret is too short. Consider using a longer secret.");
        }
        byte[] secretBytes = Base64.getEncoder().encode(secret.getBytes());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("auth", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .collect(Collectors.joining(",")));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String jwt) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token");
        } catch (SignatureException e) {
            log.error("JWT signature does not match: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT signature does not match");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new JwtAuthenticationException("JWT claims string is empty");
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        String auth = claims.get("auth", String.class);

        List<GrantedAuthority> authorities = Arrays.stream(auth.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }

    public String refreshToken(String oldToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(oldToken)
                    .getBody();

            String username = claims.getSubject();
            String auth = claims.get("auth", String.class);

            List<GrantedAuthority> authorities = Arrays.stream(auth.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return createToken(username, authorities);
        } catch (ExpiredJwtException e) {
            // Allow refresh of expired token
            String username = e.getClaims().getSubject();
            String auth = e.getClaims().get("auth", String.class);

            List<GrantedAuthority> authorities = Arrays.stream(auth.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return createToken(username, authorities);
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid refresh token");
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}