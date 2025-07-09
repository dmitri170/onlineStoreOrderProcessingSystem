package com.example.OrderService.dto;

import com.example.OrderService.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
}
