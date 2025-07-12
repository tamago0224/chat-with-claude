package com.chatapp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private AuthController.RegisterRequest validRegisterRequest;
    private AuthController.LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new AuthController.RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setName("Test User");

        validLoginRequest = new AuthController.LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
    }

    @Test
    void register_ValidRequest_ShouldReturnToken() throws Exception {
        // Given
        when(userService.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("hashedPassword");
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString(), any())).thenReturn("test-token");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"));

        verify(userService).findByEmail("test@example.com");
        verify(userService).save(any(User.class));
        verify(jwtUtil).generateToken(anyString(), eq("test@example.com"), eq("Test User"), isNull());
    }

    @Test
    void register_ExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("このメールアドレスは既に使用されています"));

        verify(userService).findByEmail("test@example.com");
        verify(userService, never()).save(any());
    }

    @Test
    void register_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        validRegisterRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        validRegisterRequest.setPassword("short");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_BlankName_ShouldReturnBadRequest() throws Exception {
        // Given
        validRegisterRequest.setName("");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getId(), testUser.getEmail(), testUser.getName(), testUser.getPicture()))
                .thenReturn("test-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.user.id").value("test-user-id"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"));

        verify(userService).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtUtil).generateToken("test-user-id", "test@example.com", "Test User", null);
    }

    @Test
    void login_InvalidEmail_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("メールアドレスまたはパスワードが正しくありません"));

        verify(userService).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("メールアドレスまたはパスワードが正しくありません"));

        verify(userService).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString(), any());
    }

    @Test
    void getCurrentUser_ValidToken_ShouldReturnUser() throws Exception {
        // Given
        String token = "Bearer valid-token";
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
        when(jwtUtil.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        when(jwtUtil.getNameFromToken("valid-token")).thenReturn("Test User");
        when(jwtUtil.getPictureFromToken("valid-token")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-user-id"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getUserIdFromToken("valid-token");
    }

    @Test
    void getCurrentUser_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        String token = "Bearer invalid-token";
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnValidTrue() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("token", "valid-token");
        
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
        when(jwtUtil.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        when(jwtUtil.getNameFromToken("valid-token")).thenReturn("Test User");
        when(jwtUtil.getPictureFromToken("valid-token")).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value("test-user-id"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnValidFalse() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("token", "invalid-token");
        
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));

        verify(jwtUtil).validateToken("invalid-token");
    }

    @Test
    void logout_ShouldReturnSuccessMessage() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ログアウトしました"));
    }

    @Test
    void refreshToken_ValidToken_ShouldReturnNewToken() throws Exception {
        // Given
        String token = "Bearer valid-token";
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
        when(jwtUtil.getEmailFromToken("valid-token")).thenReturn("test@example.com");
        when(jwtUtil.getNameFromToken("valid-token")).thenReturn("Test User");
        when(jwtUtil.getPictureFromToken("valid-token")).thenReturn(null);
        when(jwtUtil.generateToken("test-user-id", "test@example.com", "Test User", null))
                .thenReturn("new-token");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).generateToken("test-user-id", "test@example.com", "Test User", null);
    }

    @Test
    void refreshToken_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        String token = "Bearer invalid-token";
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString(), any());
    }
}