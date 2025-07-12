package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.ChatRoomService;
import com.chatapp.service.MessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class MessageController {

  @Autowired private MessageService messageService;

  @Autowired private ChatRoomService chatRoomService;

  @Autowired private JwtUtil jwtUtil;

  @GetMapping("/room/{roomId}")
  public ResponseEntity<Page<Message>> getMessagesByRoom(
      @PathVariable String roomId,
      Pageable pageable,
      @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if user has access to the room
    if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<Message> messages = messageService.findByRoomId(roomId, pageable);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/room/{roomId}/recent")
  public ResponseEntity<List<Message>> getRecentMessages(
      @PathVariable String roomId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
      @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if user has access to the room
    if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    List<Message> messages = messageService.findRecentMessages(roomId, since);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/room/{roomId}/search")
  public ResponseEntity<Page<Message>> searchMessages(
      @PathVariable String roomId,
      @RequestParam String q,
      Pageable pageable,
      @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if user has access to the room
    if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<Message> messages = messageService.searchMessages(roomId, q, pageable);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/room/{roomId}/date-range")
  public ResponseEntity<Page<Message>> getMessagesByDateRange(
      @PathVariable String roomId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      Pageable pageable,
      @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if user has access to the room
    if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<Message> messages =
        messageService.findMessagesByDateRange(roomId, startDate, endDate, pageable);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<Message>> getMessagesByUser(
      @PathVariable String userId,
      Pageable pageable,
      @RequestHeader("Authorization") String token) {
    String requestingUserId = getUserIdFromToken(token);
    if (requestingUserId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Only allow users to see their own messages or if they are in the same rooms
    if (!requestingUserId.equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<Message> messages = messageService.findByUserId(userId, pageable);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Message> getMessageById(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return messageService
        .findById(id)
        .map(
            message -> {
              // Check if user has access to the room containing this message
              if (!chatRoomService.isUserMemberOfRoom(userId, message.getRoom().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Message>build();
              }
              return ResponseEntity.ok(message);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/room/{roomId}/stats")
  public ResponseEntity<Map<String, Object>> getMessageStats(
      @PathVariable String roomId, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if user has access to the room
    if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    long totalMessages = messageService.getMessageCount(roomId);
    long recentMessages =
        messageService.getRecentMessageCount(roomId, LocalDateTime.now().minusDays(1));

    Map<String, Object> stats =
        Map.of(
            "totalMessages", totalMessages,
            "messagesLast24Hours", recentMessages);

    return ResponseEntity.ok(stats);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMessage(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return messageService
        .findById(id)
        .map(
            message -> {
              // Only allow the message author or room owner to delete messages
              boolean isAuthor = message.getUser().getId().equals(userId);
              boolean isRoomOwner = message.getRoom().getOwner().getId().equals(userId);

              if (!isAuthor && !isRoomOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
              }

              messageService.deleteMessage(id);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private String getUserIdFromToken(String token) {
    if (token == null || !token.startsWith("Bearer ")) {
      return null;
    }

    String jwtToken = token.substring(7);
    if (!jwtUtil.validateToken(jwtToken)) {
      return null;
    }

    return jwtUtil.getUserIdFromToken(jwtToken);
  }
}
