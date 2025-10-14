package com.example.OrderService.service;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Юнит тесты для сервиса пользователей.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setEmail("new@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setConfirmPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("existinguser");
        validLoginRequest.setPassword("password123");

        existingUser = new User("existinguser", "encodedPassword", "existing@example.com", Role.USER);
        existingUser.setId(1L);
    }

    @Test
    void registerUser_WithValidData_ShouldSuccess() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        ResponseEntity<?> response = userService.registerUser(validRegisterRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(userRepository, times(1)).findByEmail("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldReturnError() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(existingUser));

        // Act
        ResponseEntity<?> response = userService.registerUser(validRegisterRequest);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithPasswordMismatch_ShouldReturnError() {
        // Arrange
        validRegisterRequest.setConfirmPassword("differentPassword");

        // Act
        ResponseEntity<?> response = userService.registerUser(validRegisterRequest);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createToken(anyString(), any())).thenReturn("jwt-token");

        // Act
        ResponseEntity<?> response = userService.login(validLoginRequest);

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, times(1)).createToken(anyString(), any());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnError() {
        // Arrange
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        validLoginRequest.setPassword("wrongpassword");

        // Act
        ResponseEntity<?> response = userService.login(validLoginRequest);

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
        verify(jwtTokenProvider, never()).createToken(anyString(), any());
    }

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act
        Optional<User> result = userService.findByUsername("existinguser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(existingUser, result.get());
        verify(userRepository, times(1)).findByUsername("existinguser");
    }
}