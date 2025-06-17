package com.chatapp.controller;

import com.chatapp.security.JwtUtil;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            
            if (!jwtUtil.validateToken(jwtToken)) {
                return ResponseEntity.unauthorized().build();
            }

            String userId = jwtUtil.getUserIdFromToken(jwtToken);
            String email = jwtUtil.getEmailFromToken(jwtToken);
            String name = jwtUtil.getNameFromToken(jwtToken);
            String picture = jwtUtil.getPictureFromToken(jwtToken);

            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("email", email);
            user.put("name", name);
            user.put("picture", picture);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.unauthorized().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null || !jwtUtil.validateToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", jwtUtil.getUserIdFromToken(token));
            response.put("email", jwtUtil.getEmailFromToken(token));
            response.put("name", jwtUtil.getNameFromToken(token));
            response.put("picture", jwtUtil.getPictureFromToken(token));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since we're using stateless JWT, logout is handled on the client side
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
}