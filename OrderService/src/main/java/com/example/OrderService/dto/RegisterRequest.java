
package com.example.OrderService.dto;

import com.example.OrderService.entity.Role;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
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
    @Size(min = 6, max = 20)
    private String confirmPassword;

    private Role role;
}
