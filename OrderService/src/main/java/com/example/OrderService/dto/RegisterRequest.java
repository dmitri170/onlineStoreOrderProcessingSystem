package com.example.OrderService.dto;

import com.example.OrderService.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Size(min=3,max=15)
    private String username;

    @NotBlank
    @Size(max=40)
    @Email
    private String email;

    @NotBlank
    @Size(min=6,max=20)
    private String password;

    @NotBlank
    @Size(min=6,max=20)
    private String confirmPassword;
    private Role role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}