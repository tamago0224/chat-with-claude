package com.chatapp.controller;

import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuthController {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  // Request DTOs
  public static class RegisterRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;

    @NotBlank(message = "名前は必須です")
    @Size(min = 1, max = 255, message = "名前は1文字以上255文字以下で入力してください")
    private String name;

    // Getters and setters
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class LoginRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    private String password;

    // Getters and setters
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
    try {
      // Check if email already exists
      if (userService.findByEmail(request.getEmail()).isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "このメールアドレスは既に使用されています");
        return ResponseEntity.status(409).body(response);
      }

      // Create new user
      String userId = UUID.randomUUID().toString();
      String hashedPassword = passwordEncoder.encode(request.getPassword());

      User user = new User(userId, request.getEmail(), request.getName(), null, hashedPassword);
      userService.save(user);

      // Generate JWT token
      String token = jwtUtil.generateToken(userId, request.getEmail(), request.getName(), null);

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", userId);
      userInfo.put("email", request.getEmail());
      userInfo.put("name", request.getName());
      userInfo.put("picture", null);
      response.put("user", userInfo);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "ユーザー登録に失敗しました");
      return ResponseEntity.status(500).body(response);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
    try {
      // Find user by email
      User user = userService.findByEmail(request.getEmail()).orElse(null);

      if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "メールアドレスまたはパスワードが正しくありません");
        return ResponseEntity.status(401).body(response);
      }

      // Generate JWT token
      String token =
          jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName(), user.getPicture());

      Map<String, Object> response = new HashMap<>();
      response.put("token", token);
      Map<String, Object> userInfo = new HashMap<>();
      userInfo.put("id", user.getId());
      userInfo.put("email", user.getEmail());
      userInfo.put("name", user.getName());
      userInfo.put("picture", user.getPicture());
      response.put("user", userInfo);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "ログインに失敗しました");
      return ResponseEntity.status(500).body(response);
    }
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getCurrentUser(
      @RequestHeader("Authorization") String token) {
    try {
      String jwtToken = token.replace("Bearer ", "");

      if (!jwtUtil.validateToken(jwtToken)) {
        return ResponseEntity.status(401).build();
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
      return ResponseEntity.status(401).build();
    }
  }

  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validateToken(
      @RequestBody Map<String, String> request) {
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
    response.put("message", "ログアウトしました");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refreshToken(
      @RequestHeader("Authorization") String token) {
    try {
      String jwtToken = token.replace("Bearer ", "");

      if (!jwtUtil.validateToken(jwtToken)) {
        return ResponseEntity.status(401).build();
      }

      // Generate new token with same user data
      String userId = jwtUtil.getUserIdFromToken(jwtToken);
      String email = jwtUtil.getEmailFromToken(jwtToken);
      String name = jwtUtil.getNameFromToken(jwtToken);
      String picture = jwtUtil.getPictureFromToken(jwtToken);

      String newToken = jwtUtil.generateToken(userId, email, name, picture);

      Map<String, Object> response = new HashMap<>();
      response.put("token", newToken);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(401).build();
    }
  }
}
