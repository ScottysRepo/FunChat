package com.demo.funchat.service;

import com.demo.funchat.entity.UserEntity;
import com.demo.funchat.repository.UserRepository;
import com.demo.funchat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // Register new user
    public UserEntity register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    //Login authentication, returns a JWT token
    public String login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> jwtUtil.generateToken(user.getUsername()))
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
    }
}
