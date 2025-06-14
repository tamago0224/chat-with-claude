package com.chatapp.controller;

import com.chatapp.service.JwtService;
import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/success")
    public ResponseEntity<?> authSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        String googleId = oauth2User.getAttribute("sub");
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtService.generateToken(user.getId());
        
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "picture", user.getPicture()
                )
        ));
    }

    @GetMapping("/failure")
    public ResponseEntity<?> authFailure() {
        return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "picture", user.getPicture()
            ));
        } catch (Exception e) {
            return ResponseEntity.unauthorized().body(Map.of("error", "Invalid token"));
        }
    }
}