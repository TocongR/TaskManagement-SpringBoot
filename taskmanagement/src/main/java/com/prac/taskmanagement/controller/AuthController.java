package com.prac.taskmanagement.controller;

import com.prac.taskmanagement.dto.RegisterRequest;
import com.prac.taskmanagement.exception.UsernameAlreadyExistsException;
import com.prac.taskmanagement.model.User;
import com.prac.taskmanagement.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepo.existsByUsername(registerRequest.getUsername())) {
           throw new UsernameAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }

        User user = new User();

        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepo.save(user);

        return ResponseEntity.ok().body("User created successfully");
    }
}
