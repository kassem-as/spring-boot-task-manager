package com.example.taskmanager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username ist erforderlich")
    @Size(min = 3, max = 20, message = "Username muss zwischen 3 und 20 Zeichen lang sein")
    private String username;

    @NotBlank(message = "Email ist erforderlich")
    @Email(message = "Ungültige Email-Adresse")
    private String email;

    @NotBlank(message = "Passwort ist erforderlich")
    @Size(min = 6, message = "Passwort muss mindesten 6 Zeichen lang sein")
    private String password;
}
