package com.chatapp.controller;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.RoomMember;
import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.ChatRoomService;
import com.chatapp.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ChatRoomController {

  @Autowired private ChatRoomService chatRoomService;

  @Autowired private UserService userService;

  @Autowired private JwtUtil jwtUtil;

  @GetMapping("/public")
  public ResponseEntity<Page<ChatRoom>> getPublicRooms(Pageable pageable) {
    Page<ChatRoom> rooms = chatRoomService.findPublicRooms(pageable);
    return ResponseEntity.ok(rooms);
  }

  @GetMapping("/my")
  public ResponseEntity<List<ChatRoom>> getMyRooms(@RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    List<ChatRoom> rooms = chatRoomService.findByUserId(userId);
    return ResponseEntity.ok(rooms);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ChatRoom> getRoomById(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return chatRoomService
        .findById(id)
        .map(
            room -> {
              // Check if user has access to the room
              if (!room.getIsPrivate() || chatRoomService.isUserMemberOfRoom(userId, id)) {
                return ResponseEntity.ok(room);
              } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<ChatRoom>build();
              }
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<ChatRoom> createRoom(
      @RequestBody CreateRoomRequest request, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User owner = userService.findById(userId).orElse(null);
    if (owner == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Check if room name already exists
    if (chatRoomService.existsByName(request.getName())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    ChatRoom room = new ChatRoom();
    room.setId(UUID.randomUUID().toString());
    room.setName(request.getName());
    room.setDescription(request.getDescription());
    room.setOwner(owner);
    room.setIsPrivate(request.getIsPrivate());

    ChatRoom createdRoom = chatRoomService.createRoom(room);

    // Add owner as member
    chatRoomService.addUserToRoom(userId, createdRoom.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ChatRoom> updateRoom(
      @PathVariable String id,
      @RequestBody UpdateRoomRequest request,
      @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return chatRoomService
        .findById(id)
        .map(
            room -> {
              // Check if user is the owner
              if (!room.getOwner().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<ChatRoom>build();
              }

              if (request.getName() != null) {
                room.setName(request.getName());
              }
              if (request.getDescription() != null) {
                room.setDescription(request.getDescription());
              }
              if (request.getIsPrivate() != null) {
                room.setIsPrivate(request.getIsPrivate());
              }

              ChatRoom updatedRoom = chatRoomService.updateRoom(room);
              return ResponseEntity.ok(updatedRoom);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<Map<String, String>> joinRoom(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    ChatRoom room = chatRoomService.findById(id).orElse(null);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }

    // Check if room is private
    if (room.getIsPrivate()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Check if already a member
    if (chatRoomService.isUserMemberOfRoom(userId, id)) {
      return ResponseEntity.ok(Map.of("message", "Already a member"));
    }

    chatRoomService.addUserToRoom(userId, id);
    return ResponseEntity.ok(Map.of("message", "Joined room successfully"));
  }

  @PostMapping("/{id}/leave")
  public ResponseEntity<Map<String, String>> leaveRoom(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    ChatRoom room = chatRoomService.findById(id).orElse(null);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }

    // Don't allow owner to leave
    if (room.getOwner().getId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("message", "Owner cannot leave the room"));
    }

    chatRoomService.removeUserFromRoom(userId, id);
    return ResponseEntity.ok(Map.of("message", "Left room successfully"));
  }

  @GetMapping("/{id}/members")
  public ResponseEntity<List<RoomMember>> getRoomMembers(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    ChatRoom room = chatRoomService.findById(id).orElse(null);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }

    // Check if user has access to the room
    if (room.getIsPrivate() && !chatRoomService.isUserMemberOfRoom(userId, id)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    List<RoomMember> members = chatRoomService.getRoomMembers(id);
    return ResponseEntity.ok(members);
  }

  @GetMapping("/search")
  public ResponseEntity<Page<ChatRoom>> searchPublicRooms(
      @RequestParam String q, Pageable pageable) {
    Page<ChatRoom> rooms = chatRoomService.searchPublicRooms(q, pageable);
    return ResponseEntity.ok(rooms);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRoom(
      @PathVariable String id, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return chatRoomService
        .findById(id)
        .map(
            room -> {
              // Check if user is the owner
              if (!room.getOwner().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
              }

              chatRoomService.deleteRoom(id);
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

  // Request DTOs
  public static class CreateRoomRequest {
    private String name;
    private String description;
    private Boolean isPrivate = false;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Boolean getIsPrivate() {
      return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
      this.isPrivate = isPrivate;
    }
  }

  public static class UpdateRoomRequest {
    private String name;
    private String description;
    private Boolean isPrivate;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Boolean getIsPrivate() {
      return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
      this.isPrivate = isPrivate;
    }
  }
}
