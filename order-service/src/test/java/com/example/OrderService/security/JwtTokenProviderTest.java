package com.example.OrderService.security;

import com.example.OrderService.exception.JwtAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юнит тесты для JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "very-long-secret-key-for-testing-purposes-1234567890";
    private Collection<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "validityInMilliseconds", 3600000L);
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");

        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void createToken_WithValidData_ShouldReturnToken() {
        // Act
        String token = jwtTokenProvider.createToken("testuser", authorities);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.createToken("testuser", authorities);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtAuthenticationException.class, () -> {
            jwtTokenProvider.validateToken(invalidToken);
        });
    }

    @Test
    void getUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtTokenProvider.createToken("testuser", authorities);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() {
        // Arrange
        String originalToken = jwtTokenProvider.createToken("testuser", authorities);

        // Act
        String refreshedToken = jwtTokenProvider.refreshToken(originalToken);

        // Assert
        assertNotNull(refreshedToken);
        assertNotEquals(originalToken, refreshedToken);
    }
}