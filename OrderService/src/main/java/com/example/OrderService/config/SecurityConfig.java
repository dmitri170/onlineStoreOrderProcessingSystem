package com.example.OrderService.config;

import com.example.OrderService.security.JwtTokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
}
