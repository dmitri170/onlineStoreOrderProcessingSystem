package com.example.OrderService.controller;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;



    @PostMapping("/reg")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        return userService.refreshToken(token);
    }
}