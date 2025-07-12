package com.chatapp.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.RoomMember;
import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.ChatRoomService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatRoomController.class)
class ChatRoomControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ChatRoomService chatRoomService;

  @MockBean private UserService userService;

  @MockBean private JwtUtil jwtUtil;

  private User testUser;
  private ChatRoom testRoom;
  private ChatRoomController.CreateRoomRequest createRoomRequest;
  private ChatRoomController.UpdateRoomRequest updateRoomRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("test-user-id");
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");

    testRoom = new ChatRoom();
    testRoom.setId("test-room-id");
    testRoom.setName("Test Room");
    testRoom.setDescription("Test Description");
    testRoom.setOwner(testUser);
    testRoom.setIsPrivate(false);

    createRoomRequest = new ChatRoomController.CreateRoomRequest();
    createRoomRequest.setName("New Room");
    createRoomRequest.setDescription("New Room Description");
    createRoomRequest.setIsPrivate(false);

    updateRoomRequest = new ChatRoomController.UpdateRoomRequest();
    updateRoomRequest.setName("Updated Room");
    updateRoomRequest.setDescription("Updated Description");
    updateRoomRequest.setIsPrivate(true);
  }

  private void mockValidToken() {
    when(jwtUtil.validateToken("valid-token")).thenReturn(true);
    when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
  }

  @Test
  void getPublicRooms_ShouldReturnPageOfRooms() throws Exception {
    // Given
    Page<ChatRoom> rooms = new PageImpl<>(Arrays.asList(testRoom));
    when(chatRoomService.findPublicRooms(any(Pageable.class))).thenReturn(rooms);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value("test-room-id"))
        .andExpect(jsonPath("$.content[0].name").value("Test Room"));

    verify(chatRoomService).findPublicRooms(any(Pageable.class));
  }

  @Test
  void getMyRooms_ValidToken_ShouldReturnUserRooms() throws Exception {
    // Given
    mockValidToken();
    List<ChatRoom> rooms = Arrays.asList(testRoom);
    when(chatRoomService.findByUserId("test-user-id")).thenReturn(rooms);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/my").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("test-room-id"))
        .andExpect(jsonPath("$[0].name").value("Test Room"));

    verify(chatRoomService).findByUserId("test-user-id");
  }

  @Test
  void getMyRooms_InvalidToken_ShouldReturnUnauthorized() throws Exception {
    // Given
    when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/my").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());

    verify(chatRoomService, never()).findByUserId(anyString());
  }

  @Test
  void getRoomById_PublicRoom_ShouldReturnRoom() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(get("/api/rooms/test-room-id").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-room-id"))
        .andExpect(jsonPath("$.name").value("Test Room"));

    verify(chatRoomService).findById("test-room-id");
  }

  @Test
  void getRoomById_PrivateRoomUserIsMember_ShouldReturnRoom() throws Exception {
    // Given
    mockValidToken();
    testRoom.setIsPrivate(true);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/test-room-id").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-room-id"));

    verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
  }

  @Test
  void getRoomById_PrivateRoomUserNotMember_ShouldReturnForbidden() throws Exception {
    // Given
    mockValidToken();
    testRoom.setIsPrivate(true);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(false);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/test-room-id").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isForbidden());

    verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
  }

  @Test
  void getRoomById_RoomNotFound_ShouldReturnNotFound() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("nonexistent-room")).thenReturn(Optional.empty());

    // When & Then
    mockMvc
        .perform(get("/api/rooms/nonexistent-room").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isNotFound());

    verify(chatRoomService).findById("nonexistent-room");
  }

  @Test
  void createRoom_ValidRequest_ShouldCreateRoom() throws Exception {
    // Given
    mockValidToken();
    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));
    when(chatRoomService.existsByName("New Room")).thenReturn(false);
    when(chatRoomService.createRoom(any(ChatRoom.class))).thenReturn(testRoom);

    // When & Then
    mockMvc
        .perform(
            post("/api/rooms")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("test-room-id"));

    verify(chatRoomService).existsByName("New Room");
    verify(chatRoomService).createRoom(any(ChatRoom.class));
    verify(chatRoomService).addUserToRoom("test-user-id", "test-room-id");
  }

  @Test
  void createRoom_DuplicateName_ShouldReturnConflict() throws Exception {
    // Given
    mockValidToken();
    when(userService.findById("test-user-id")).thenReturn(Optional.of(testUser));
    when(chatRoomService.existsByName("New Room")).thenReturn(true);

    // When & Then
    mockMvc
        .perform(
            post("/api/rooms")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomRequest)))
        .andExpect(status().isConflict());

    verify(chatRoomService).existsByName("New Room");
    verify(chatRoomService, never()).createRoom(any());
  }

  @Test
  void updateRoom_ValidOwner_ShouldUpdateRoom() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.updateRoom(any(ChatRoom.class))).thenReturn(testRoom);

    // When & Then
    mockMvc
        .perform(
            put("/api/rooms/test-room-id")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-room-id"));

    verify(chatRoomService).updateRoom(any(ChatRoom.class));
  }

  @Test
  void updateRoom_NotOwner_ShouldReturnForbidden() throws Exception {
    // Given
    mockValidToken();
    User otherUser = new User();
    otherUser.setId("other-user-id");
    testRoom.setOwner(otherUser);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(
            put("/api/rooms/test-room-id")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRoomRequest)))
        .andExpect(status().isForbidden());

    verify(chatRoomService, never()).updateRoom(any());
  }

  @Test
  void joinRoom_PublicRoom_ShouldJoinSuccessfully() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(false);

    // When & Then
    mockMvc
        .perform(post("/api/rooms/test-room-id/join").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Joined room successfully"));

    verify(chatRoomService).addUserToRoom("test-user-id", "test-room-id");
  }

  @Test
  void joinRoom_PrivateRoom_ShouldReturnForbidden() throws Exception {
    // Given
    mockValidToken();
    testRoom.setIsPrivate(true);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(post("/api/rooms/test-room-id/join").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isForbidden());

    verify(chatRoomService, never()).addUserToRoom(anyString(), anyString());
  }

  @Test
  void joinRoom_AlreadyMember_ShouldReturnAlreadyMemberMessage() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);

    // When & Then
    mockMvc
        .perform(post("/api/rooms/test-room-id/join").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Already a member"));

    verify(chatRoomService, never()).addUserToRoom(anyString(), anyString());
  }

  @Test
  void leaveRoom_NonOwner_ShouldLeaveSuccessfully() throws Exception {
    // Given
    mockValidToken();
    User otherUser = new User();
    otherUser.setId("other-user-id");
    testRoom.setOwner(otherUser);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(
            post("/api/rooms/test-room-id/leave").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Left room successfully"));

    verify(chatRoomService).removeUserFromRoom("test-user-id", "test-room-id");
  }

  @Test
  void leaveRoom_Owner_ShouldReturnConflict() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(
            post("/api/rooms/test-room-id/leave").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Owner cannot leave the room"));

    verify(chatRoomService, never()).removeUserFromRoom(anyString(), anyString());
  }

  @Test
  void getRoomMembers_HasAccess_ShouldReturnMembers() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);

    RoomMember member = new RoomMember();
    List<RoomMember> members = Arrays.asList(member);
    when(chatRoomService.getRoomMembers("test-room-id")).thenReturn(members);

    // When & Then
    mockMvc
        .perform(
            get("/api/rooms/test-room-id/members").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());

    verify(chatRoomService).getRoomMembers("test-room-id");
  }

  @Test
  void getRoomMembers_PrivateRoomNoAccess_ShouldReturnForbidden() throws Exception {
    // Given
    mockValidToken();
    testRoom.setIsPrivate(true);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));
    when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(false);

    // When & Then
    mockMvc
        .perform(
            get("/api/rooms/test-room-id/members").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isForbidden());

    verify(chatRoomService, never()).getRoomMembers(anyString());
  }

  @Test
  void searchPublicRooms_ShouldReturnMatchingRooms() throws Exception {
    // Given
    Page<ChatRoom> rooms = new PageImpl<>(Arrays.asList(testRoom));
    when(chatRoomService.searchPublicRooms(eq("Test"), any(Pageable.class))).thenReturn(rooms);

    // When & Then
    mockMvc
        .perform(get("/api/rooms/search").param("q", "Test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value("test-room-id"));

    verify(chatRoomService).searchPublicRooms(eq("Test"), any(Pageable.class));
  }

  @Test
  void deleteRoom_Owner_ShouldDeleteRoom() throws Exception {
    // Given
    mockValidToken();
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(delete("/api/rooms/test-room-id").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isNoContent());

    verify(chatRoomService).deleteRoom("test-room-id");
  }

  @Test
  void deleteRoom_NotOwner_ShouldReturnForbidden() throws Exception {
    // Given
    mockValidToken();
    User otherUser = new User();
    otherUser.setId("other-user-id");
    testRoom.setOwner(otherUser);
    when(chatRoomService.findById("test-room-id")).thenReturn(Optional.of(testRoom));

    // When & Then
    mockMvc
        .perform(delete("/api/rooms/test-room-id").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isForbidden());

    verify(chatRoomService, never()).deleteRoom(anyString());
  }
}
