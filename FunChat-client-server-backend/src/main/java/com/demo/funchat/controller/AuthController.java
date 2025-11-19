package com.demo.funchat.controller;

import com.demo.funchat.entity.UserEntity;
import com.demo.funchat.repository.UserRepository;
import com.demo.funchat.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password) {
        UserEntity user = authService.register(username, password);
        return ResponseEntity.ok(new java.util.HashMap<>() {{
            put("id", user.getId());
            put("username", user.getUsername());
        }});
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        String token = authService.login(username, password);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after login"));

        return ResponseEntity.ok(new java.util.HashMap<>() {{
            put("token", token);
            put("id", user.getId());
            put("username", user.getUsername());
        }});
    }
}
