package com.example.OrderService.controller;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @RequestMapping("/reg")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body("Username already taken");
        }
        Role role=request.getRole()!=null?request.getRole():Role.USER;
        User user=User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("User registered with role: " + role);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
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
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String oldToken = request.get("token");
        try {
            String newToken = jwtTokenProvider.refreshToken(oldToken);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

}
