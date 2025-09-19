package com.example.OrderService.service;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
        if (findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("User registered with role: " + role);
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtTokenProvider.createToken(
                user.getUsername(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        return ResponseEntity.ok(Map.of("token", token));
    }

    public ResponseEntity<?> refreshToken(String oldToken) {
        try {
            String newToken = jwtTokenProvider.refreshToken(oldToken);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }
}