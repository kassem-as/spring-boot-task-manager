package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verificationLink = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your Task Manager account");
        message.setText(
                "Hi " + username + ",\n\n" +
                        "Please verify your email address by clicking the link below:\n\n" +
                        verificationLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "If you did not register, please ignore this email.\n\n" +
                        "Task Manager Team"
        );

        mailSender.send(message);
    }
}
