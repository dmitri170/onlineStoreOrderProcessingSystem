package com.example.OrderService.dto;

import com.example.OrderService.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Size(max = 40, message = "Email must not exceed 40 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private Role role = Role.USER;
}