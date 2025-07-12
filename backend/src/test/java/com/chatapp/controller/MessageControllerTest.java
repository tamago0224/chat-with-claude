package com.chatapp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.Message;
import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.ChatRoomService;
import com.chatapp.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private ChatRoom testRoom;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testRoom = new ChatRoom();
        testRoom.setId("test-room-id");
        testRoom.setName("Test Room");
        testRoom.setOwner(testUser);

        testMessage = new Message();
        testMessage.setId("test-message-id");
        testMessage.setContent("Test message content");
        testMessage.setUser(testUser);
        testMessage.setRoom(testRoom);
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
    }

    @Test
    void getMessagesByRoom_UserIsMember_ShouldReturnMessages() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);
        Page<Message> messages = new PageImpl<>(Arrays.asList(testMessage));
        when(messageService.findByRoomId(eq("test-room-id"), any(Pageable.class))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("test-message-id"))
                .andExpect(jsonPath("$.content[0].content").value("Test message content"));

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService).findByRoomId(eq("test-room-id"), any(Pageable.class));
    }

    @Test
    void getMessagesByRoom_UserNotMember_ShouldReturnForbidden() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService, never()).findByRoomId(anyString(), any(Pageable.class));
    }

    @Test
    void getMessagesByRoom_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(chatRoomService, never()).isUserMemberOfRoom(anyString(), anyString());
        verify(messageService, never()).findByRoomId(anyString(), any(Pageable.class));
    }

    @Test
    void getRecentMessages_UserIsMember_ShouldReturnMessages() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);
        List<Message> messages = Arrays.asList(testMessage);
        when(messageService.findRecentMessages(eq("test-room-id"), any(LocalDateTime.class))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id/recent")
                .param("since", "2024-01-01T00:00:00")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("test-message-id"));

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService).findRecentMessages(eq("test-room-id"), any(LocalDateTime.class));
    }

    @Test
    void searchMessages_UserIsMember_ShouldReturnMatchingMessages() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);
        Page<Message> messages = new PageImpl<>(Arrays.asList(testMessage));
        when(messageService.searchMessages(eq("test-room-id"), eq("test"), any(Pageable.class))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id/search")
                .param("q", "test")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("test-message-id"));

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService).searchMessages(eq("test-room-id"), eq("test"), any(Pageable.class));
    }

    @Test
    void getMessagesByDateRange_UserIsMember_ShouldReturnMessages() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);
        Page<Message> messages = new PageImpl<>(Arrays.asList(testMessage));
        when(messageService.findMessagesByDateRange(eq("test-room-id"), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id/date-range")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("test-message-id"));

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService).findMessagesByDateRange(eq("test-room-id"), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getMessagesByUser_SameUser_ShouldReturnMessages() throws Exception {
        // Given
        mockValidToken();
        Page<Message> messages = new PageImpl<>(Arrays.asList(testMessage));
        when(messageService.findByUserId(eq("test-user-id"), any(Pageable.class))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/user/test-user-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("test-message-id"));

        verify(messageService).findByUserId(eq("test-user-id"), any(Pageable.class));
    }

    @Test
    void getMessagesByUser_DifferentUser_ShouldReturnForbidden() throws Exception {
        // Given
        mockValidToken();

        // When & Then
        mockMvc.perform(get("/api/messages/user/other-user-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());

        verify(messageService, never()).findByUserId(anyString(), any(Pageable.class));
    }

    @Test
    void getMessageById_UserHasAccess_ShouldReturnMessage() throws Exception {
        // Given
        mockValidToken();
        when(messageService.findById("test-message-id")).thenReturn(Optional.of(testMessage));
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/messages/test-message-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-message-id"))
                .andExpect(jsonPath("$.content").value("Test message content"));

        verify(messageService).findById("test-message-id");
        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
    }

    @Test
    void getMessageById_UserNoAccess_ShouldReturnForbidden() throws Exception {
        // Given
        mockValidToken();
        when(messageService.findById("test-message-id")).thenReturn(Optional.of(testMessage));
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/messages/test-message-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());

        verify(messageService).findById("test-message-id");
        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
    }

    @Test
    void getMessageById_MessageNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        mockValidToken();
        when(messageService.findById("nonexistent-message")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/messages/nonexistent-message")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());

        verify(messageService).findById("nonexistent-message");
        verify(chatRoomService, never()).isUserMemberOfRoom(anyString(), anyString());
    }

    @Test
    void getMessageStats_UserIsMember_ShouldReturnStats() throws Exception {
        // Given
        mockValidToken();
        when(chatRoomService.isUserMemberOfRoom("test-user-id", "test-room-id")).thenReturn(true);
        when(messageService.getMessageCount("test-room-id")).thenReturn(100L);
        when(messageService.getRecentMessageCount(eq("test-room-id"), any(LocalDateTime.class))).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/messages/room/test-room-id/stats")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(100))
                .andExpect(jsonPath("$.messagesLast24Hours").value(10));

        verify(chatRoomService).isUserMemberOfRoom("test-user-id", "test-room-id");
        verify(messageService).getMessageCount("test-room-id");
        verify(messageService).getRecentMessageCount(eq("test-room-id"), any(LocalDateTime.class));
    }

    @Test
    void deleteMessage_AuthorCanDelete_ShouldDeleteMessage() throws Exception {
        // Given
        mockValidToken();
        when(messageService.findById("test-message-id")).thenReturn(Optional.of(testMessage));

        // When & Then
        mockMvc.perform(delete("/api/messages/test-message-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(messageService).findById("test-message-id");
        verify(messageService).deleteMessage("test-message-id");
    }

    @Test
    void deleteMessage_RoomOwnerCanDelete_ShouldDeleteMessage() throws Exception {
        // Given
        mockValidToken();
        User otherUser = new User();
        otherUser.setId("other-user-id");
        testMessage.setUser(otherUser);
        when(messageService.findById("test-message-id")).thenReturn(Optional.of(testMessage));

        // When & Then
        mockMvc.perform(delete("/api/messages/test-message-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(messageService).findById("test-message-id");
        verify(messageService).deleteMessage("test-message-id");
    }

    @Test
    void deleteMessage_NotAuthorOrOwner_ShouldReturnForbidden() throws Exception {
        // Given
        mockValidToken();
        User otherUser = new User();
        otherUser.setId("other-user-id");
        testMessage.setUser(otherUser);
        testRoom.setOwner(otherUser);
        when(messageService.findById("test-message-id")).thenReturn(Optional.of(testMessage));

        // When & Then
        mockMvc.perform(delete("/api/messages/test-message-id")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());

        verify(messageService).findById("test-message-id");
        verify(messageService, never()).deleteMessage(anyString());
    }

    @Test
    void deleteMessage_MessageNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        mockValidToken();
        when(messageService.findById("nonexistent-message")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/messages/nonexistent-message")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());

        verify(messageService).findById("nonexistent-message");
        verify(messageService, never()).deleteMessage(anyString());
    }
}