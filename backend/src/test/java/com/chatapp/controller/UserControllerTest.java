package com.chatapp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.entity.User;
import com.chatapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import com.chatapp.config.TestSecurityConfig;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class, TestSecurityConfig.class})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("test-user-id");
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");
    testUser.setPicture("https://example.com/picture.jpg");
  }

  @Test
  void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
    // Given
    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));

    // When & Then
    mockMvc
        .perform(get("/api/users/test-user-id"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-user-id"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.picture").value("https://example.com/picture.jpg"));

    verify(userService).findById("test-user-id");
  }

  @Test
  void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
    // Given
    when(userService.findById("nonexistent-user")).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/users/nonexistent-user")).andExpect(status().isNotFound());

    verify(userService).findById("nonexistent-user");
  }

  @Test
  void getUserByEmail_ExistingUser_ShouldReturnUser() throws Exception {
    // Given
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    // When & Then
    mockMvc
        .perform(get("/api/users/email/test@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-user-id"))
        .andExpect(jsonPath("$.email").value("test@example.com"));

    verify(userService).findByEmail("test@example.com");
  }

  @Test
  void getUserByEmail_NonExistingUser_ShouldReturnNotFound() throws Exception {
    // Given
    when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // When & Then
    mockMvc
        .perform(get("/api/users/email/nonexistent@example.com"))
        .andExpect(status().isNotFound());

    verify(userService).findByEmail("nonexistent@example.com");
  }

  @Test
  void getUsersByRoom_ShouldReturnUsers() throws Exception {
    // Given
    List<User> users = Arrays.asList(testUser);
    when(userService.findByRoomId("test-room-id")).thenReturn(users);

    // When & Then
    mockMvc
        .perform(get("/api/users/room/test-room-id"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("test-user-id"))
        .andExpect(jsonPath("$[0].name").value("Test User"));

    verify(userService).findByRoomId("test-room-id");
  }

  @Test
  void searchUsers_ShouldReturnMatchingUsers() throws Exception {
    // Given
    List<User> users = Arrays.asList(testUser);
    when(userService.searchUsers("Test")).thenReturn(users);

    // When & Then
    mockMvc
        .perform(get("/api/users/search").param("q", "Test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("test-user-id"))
        .andExpect(jsonPath("$[0].name").value("Test User"));

    verify(userService).searchUsers("Test");
  }

  @Test
  void updateUser_ExistingUser_ShouldUpdateAndReturnUser() throws Exception {
    // Given
    User updateRequest = new User();
    updateRequest.setName("Updated Name");
    updateRequest.setPicture("https://example.com/new-picture.jpg");

    User updatedUser = new User();
    updatedUser.setId("test-user-id");
    updatedUser.setEmail("test@example.com");
    updatedUser.setName("Updated Name");
    updatedUser.setPicture("https://example.com/new-picture.jpg");

    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));
    when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

    // When & Then
    mockMvc
        .perform(
            put("/api/users/test-user-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-user-id"))
        .andExpect(jsonPath("$.name").value("Updated Name"))
        .andExpect(jsonPath("$.picture").value("https://example.com/new-picture.jpg"));

    verify(userService).findById("test-user-id");
    verify(userService).updateUser(any(User.class));
  }

  @Test
  void updateUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
    // Given
    User updateRequest = new User();
    updateRequest.setName("Updated Name");

    when(userService.findById("nonexistent-user")).thenReturn(Optional.empty());

    // When & Then
    mockMvc
        .perform(
            put("/api/users/nonexistent-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());

    verify(userService).findById("nonexistent-user");
    verify(userService, never()).updateUser(any());
  }

  @Test
  void updateUser_PartialUpdate_ShouldOnlyUpdateProvidedFields() throws Exception {
    // Given
    User updateRequest = new User();
    updateRequest.setName("Updated Name");
    // picture is null, should not be updated

    User updatedUser = new User();
    updatedUser.setId("test-user-id");
    updatedUser.setEmail("test@example.com");
    updatedUser.setName("Updated Name");
    updatedUser.setPicture("https://example.com/picture.jpg"); // Original picture

    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));
    when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

    // When & Then
    mockMvc
        .perform(
            put("/api/users/test-user-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Name"))
        .andExpect(jsonPath("$.picture").value("https://example.com/picture.jpg"));

    verify(userService).updateUser(any(User.class));
  }

  @Test
  void getUserStats_ShouldReturnStats() throws Exception {
    // Given
    when(userService.getUserCount()).thenReturn(150L);

    // When & Then
    mockMvc
        .perform(get("/api/users/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").value(150));

    verify(userService).getUserCount();
  }

  @Test
  void deleteUser_ExistingUser_ShouldDeleteUser() throws Exception {
    // Given
    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));

    // When & Then
    mockMvc.perform(delete("/api/users/test-user-id")).andExpect(status().isNoContent());

    verify(userService).findById("test-user-id");
    verify(userService).deleteUser("test-user-id");
  }

  @Test
  void deleteUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
    // Given
    when(userService.findById("nonexistent-user")).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(delete("/api/users/nonexistent-user")).andExpect(status().isNotFound());

    verify(userService).findById("nonexistent-user");
    verify(userService, never()).deleteUser(anyString());
  }

  @Test
  void getUsersByRoom_EmptyResult_ShouldReturnEmptyArray() throws Exception {
    // Given
    when(userService.findByRoomId("empty-room")).thenReturn(Arrays.asList());

    // When & Then
    mockMvc
        .perform(get("/api/users/room/empty-room"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(userService).findByRoomId("empty-room");
  }

  @Test
  void searchUsers_EmptyQuery_ShouldReturnEmptyArray() throws Exception {
    // Given
    when(userService.searchUsers("")).thenReturn(Arrays.asList());

    // When & Then
    mockMvc
        .perform(get("/api/users/search").param("q", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(userService).searchUsers("");
  }
}
