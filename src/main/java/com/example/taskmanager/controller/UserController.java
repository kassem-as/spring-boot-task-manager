package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal User user) {
        return user;
    }
}
