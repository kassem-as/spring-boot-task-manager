package com.example.taskmanager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username ist erforderlich")
    private String username;

    @NotBlank(message = "Passwort ist erforderlich")
    private String password;
}
