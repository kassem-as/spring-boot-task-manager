package com.example.taskmanager.controller;

import com.example.taskmanager.dto.auth.AuthResponse;
import com.example.taskmanager.dto.auth.LoginRequest;
import com.example.taskmanager.dto.auth.RegisterRequest;
import com.example.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and sends a verification email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered, Verification email sent."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation error"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists"
            )
    })
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email", description = "Verfies user email address via token from email link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public String verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return "E-Mail erfolgreich verifiziert. Du kannst dich jetzt einloggen.";
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user and returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Email not verified"
            )
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
