package com.demo.funchat.controller;

import com.demo.funchat.entity.UserEntity;
import com.demo.funchat.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password) {
        UserEntity user = authService.register(username, password);
        return ResponseEntity.ok("User registered with id: " + user.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        String token = authService.login(username, password);
        return ResponseEntity.ok("JWT Token: " + token);
    }
}

