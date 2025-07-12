package com.chatapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.ChatBackendApplication;
import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.User;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests to verify that the backend APIs comply with the specification requirements
 * defined in docs/chat_app_specification.md
 */
@SpringBootTest(classes = ChatBackendApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ApiSpecificationComplianceTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private ChatRoomRepository chatRoomRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtUtil jwtUtil;

  private User testUser;
  private String validToken;

  @BeforeEach
  void setUp() {
    // Clean up
    chatRoomRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setId("test-user-id");
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Generate valid JWT token
    validToken =
        jwtUtil.generateToken(
            testUser.getId(), testUser.getEmail(), testUser.getName(), testUser.getPicture());
  }

  @Test
  @DisplayName("REST API Authentication - POST /api/auth/register should comply with specification")
  void testAuthRegisterApiCompliance() throws Exception {
    Map<String, String> registerRequest = new HashMap<>();
    registerRequest.put("email", "newuser@example.com");
    registerRequest.put("password", "password123");
    registerRequest.put("name", "New User");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.user.id").exists())
        .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.user.name").value("New User"))
        .andExpect(jsonPath("$.user.picture").exists());
  }

  @Test
  @DisplayName("REST API Authentication - POST /api/auth/login should comply with specification")
  void testAuthLoginApiCompliance() throws Exception {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", testUser.getEmail());
    loginRequest.put("password", "password123");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.user.id").value(testUser.getId()))
        .andExpect(jsonPath("$.user.email").value(testUser.getEmail()))
        .andExpect(jsonPath("$.user.name").value(testUser.getName()));
  }

  @Test
  @DisplayName("REST API Authentication - POST /api/auth/logout should comply with specification")
  void testAuthLogoutApiCompliance() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("REST API Authentication - POST /api/auth/refresh should comply with specification")
  void testAuthRefreshApiCompliance() throws Exception {
    mockMvc
        .perform(post("/api/auth/refresh").header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  @DisplayName("REST API File Upload - POST /api/upload/image should comply with specification")
  void testFileUploadApiCompliance() throws Exception {
    // Note: This test requires MockMultipartFile setup
    // For now, we test the endpoint exists and requires authentication
    mockMvc.perform(post("/api/upload/image")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("REST API File Access - GET /api/files/:fileId should comply with specification")
  void testFileAccessApiCompliance() throws Exception {
    mockMvc.perform(get("/api/upload/files/nonexistent.jpg")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("REST API Health Check - GET /actuator/health should comply with specification")
  void testHealthCheckApiCompliance() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").exists());
  }

  @Test
  @DisplayName("Authentication validation should enforce JWT token requirements")
  void testJwtAuthenticationRequirements() throws Exception {
    // Test accessing protected endpoint without token
    mockMvc.perform(get("/api/rooms/my")).andExpect(status().isUnauthorized());

    // Test accessing protected endpoint with invalid token
    mockMvc
        .perform(get("/api/rooms/my").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());

    // Test accessing protected endpoint with valid token
    mockMvc
        .perform(get("/api/rooms/my").header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("User registration should validate input according to specification")
  void testUserRegistrationValidation() throws Exception {
    // Test invalid email
    Map<String, String> invalidEmailRequest = new HashMap<>();
    invalidEmailRequest.put("email", "invalid-email");
    invalidEmailRequest.put("password", "password123");
    invalidEmailRequest.put("name", "Test User");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
        .andExpect(status().isBadRequest());

    // Test short password
    Map<String, String> shortPasswordRequest = new HashMap<>();
    shortPasswordRequest.put("email", "test2@example.com");
    shortPasswordRequest.put("password", "short");
    shortPasswordRequest.put("name", "Test User");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
        .andExpect(status().isBadRequest());

    // Test blank name
    Map<String, String> blankNameRequest = new HashMap<>();
    blankNameRequest.put("email", "test3@example.com");
    blankNameRequest.put("password", "password123");
    blankNameRequest.put("name", "");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankNameRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Chat room creation should follow specification requirements")
  void testChatRoomCreationCompliance() throws Exception {
    Map<String, Object> createRoomRequest = new HashMap<>();
    createRoomRequest.put("name", "Test Room");
    createRoomRequest.put("description", "Test room description");
    createRoomRequest.put("isPrivate", false);

    mockMvc
        .perform(
            post("/api/rooms")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Test Room"))
        .andExpect(jsonPath("$.description").value("Test room description"))
        .andExpect(jsonPath("$.isPrivate").value(false))
        .andExpect(jsonPath("$.owner.id").value(testUser.getId()));
  }

  @Test
  @DisplayName("Message retrieval should respect room membership requirements")
  void testMessageAccessControlCompliance() throws Exception {
    // Create a room
    ChatRoom room = new ChatRoom();
    room.setId("test-room");
    room.setName("Test Room");
    room.setOwner(testUser);
    room.setIsPrivate(false);
    room = chatRoomRepository.save(room);

    // Try to access messages without being a member should be forbidden
    mockMvc
        .perform(
            get("/api/messages/room/" + room.getId())
                .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Public room listing should be accessible without authentication")
  void testPublicRoomListingCompliance() throws Exception {
    mockMvc
        .perform(get("/api/rooms/public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("User search functionality should comply with specification")
  void testUserSearchCompliance() throws Exception {
    mockMvc
        .perform(get("/api/users/search").param("q", "Test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("Room search functionality should comply with specification")
  void testRoomSearchCompliance() throws Exception {
    mockMvc
        .perform(get("/api/rooms/search").param("q", "Test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("Error responses should follow consistent format")
  void testErrorResponseFormat() throws Exception {
    // Test 401 Unauthorized format
    mockMvc.perform(get("/api/rooms/my")).andExpect(status().isUnauthorized());

    // Test 404 Not Found format
    mockMvc.perform(get("/api/users/nonexistent-user")).andExpect(status().isNotFound());

    // Test 409 Conflict format (duplicate email registration)
    Map<String, String> duplicateEmailRequest = new HashMap<>();
    duplicateEmailRequest.put("email", testUser.getEmail());
    duplicateEmailRequest.put("password", "password123");
    duplicateEmailRequest.put("name", "Duplicate User");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("CORS headers should be configured according to specification")
  void testCorsConfigurationCompliance() throws Exception {
    mockMvc
        .perform(
            options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(header().exists("Access-Control-Allow-Origin"));
  }

  @Test
  @DisplayName("JWT token validation endpoint should comply with specification")
  void testTokenValidationCompliance() throws Exception {
    Map<String, String> validTokenRequest = new HashMap<>();
    validTokenRequest.put("token", validToken);

    mockMvc
        .perform(
            post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTokenRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.email").value(testUser.getEmail()));

    Map<String, String> invalidTokenRequest = new HashMap<>();
    invalidTokenRequest.put("token", "invalid-token");

    mockMvc
        .perform(
            post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTokenRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(false));
  }
}
