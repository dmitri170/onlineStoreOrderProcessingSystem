package com.example.OrderService.security;

import com.example.OrderService.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
/**
 * Компонент для работы с JWT токенами.
 * Отвечает за создание, валидацию и обновление JWT токенов аутентификации.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long validityInMilliseconds;

    private Key key;

    /**
     * Инициализирует ключ для подписи токенов после создания бина.
     */
    @PostConstruct
    protected void init() {
        if (secret.length() < 32) {
            log.warn("JWT secret is too short. Consider using a longer secret.");
        }
        byte[] secretBytes = Base64.getEncoder().encode(secret.getBytes());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }
    /**
     * Создает новый JWT токен для пользователя.
     *
     * @param username имя пользователя
     * @param authorities список прав пользователя
     * @return JWT токен в виде строки
     */
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
    /**
     * Проверяет валидность JWT токена.
     *
     * @param jwt токен для проверки
     * @return true если токен валиден, false в противном случае
     * @throws JwtAuthenticationException если токен невалиден
     */
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
    /**
     * Создает объект аутентификации из JWT токена.
     *
     * @param token JWT токен
     * @return объект аутентификации Spring Security
     */
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
    /**
     * Обновляет JWT токен.
     *
     * @param oldToken старый токен для обновления
     * @return новый JWT токен
     * @throws JwtAuthenticationException если старый токен невалиден
     */
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
    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
} 
