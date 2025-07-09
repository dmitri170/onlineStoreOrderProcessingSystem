package com.example.OrderService.controller;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @RequestMapping("/reg")
    public ResponseEntity<?> register(@RequestBody RegisterRequest register){
        if(userRepository.findByUsername(register.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body("Username already taken");
        }
        Role role=register.getRole()!=null?register.getRole():Role.USER;
        User user=User.builder()
                .username(register.getUsername())
                .email(register.getEmail())
                .password(passwordEncoder.encode(register.getPassword()))
                .role(role)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("User registered with role: " + role);
    }
    @RequestMapping("/login")
    public ResponseEntity<?> register(@RequestBody LoginRequest req){

    }
    @RequestMapping("/refresh")
    public ResponseEntity<?> register(@RequestBody String oldToken){

    }
}
