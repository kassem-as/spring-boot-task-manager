package com.example.taskmanager.service;

import com.example.taskmanager.dto.auth.AuthResponse;
import com.example.taskmanager.dto.auth.LoginRequest;
import com.example.taskmanager.dto.auth.RegisterRequest;
import com.example.taskmanager.exception.InvalidVerificationTokenException;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.VerificationToken;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.VerificationTokenRepository;
import com.example.taskmanager.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       VerificationTokenRepository verificationTokenRepository,
                       EmailService emailService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    public void register(RegisterRequest request){
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username ist bereits vergeben");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email ist bereits registriert");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token,savedUser);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), token);
    }

    @Transactional
    public void verifyEmail(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Ungültiger Verifizierungstoken"));

        if(verificationToken.isUsed()){
            throw new InvalidVerificationTokenException("Dieser Token wurde bereits verwendet");
        }
        if(verificationToken.isExpired()){
            throw new InvalidVerificationTokenException("Dieser Token ist abgelaufen, Bitte registriere dich erneut.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    public AuthResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );

    }
}
