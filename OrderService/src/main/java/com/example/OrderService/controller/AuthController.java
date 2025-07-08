package com.example.OrderService.controller;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @RequestMapping("/reg")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){

    }
    @RequestMapping("/login")
    public ResponseEntity<?> register(@RequestBody LoginRequest req){

    }
    @RequestMapping("/refresh")
    public ResponseEntity<?> register(@RequestBody String oldToken){

    }
}
