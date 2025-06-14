package com.chatapp.controller;

import com.chatapp.dto.CreateRoomRequest;
import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.User;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class ChatRoomController {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createRoom(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateRoomRequest request) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            User owner = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ChatRoom room = new ChatRoom();
            room.setId(UUID.randomUUID().toString());
            room.setName(request.getName());
            room.setDescription(request.getDescription());
            room.setOwner(owner);
            room.setIsPrivate(request.getIsPrivate());
            
            room = chatRoomRepository.save(room);
            
            return ResponseEntity.ok(Map.of(
                    "id", room.getId(),
                    "name", room.getName(),
                    "description", room.getDescription(),
                    "isPrivate", room.getIsPrivate(),
                    "owner", Map.of(
                            "id", owner.getId(),
                            "name", owner.getName(),
                            "picture", owner.getPicture()
                    ),
                    "createdAt", room.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getRooms(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            
            Pageable pageable = PageRequest.of(page, size);
            List<ChatRoom> publicRooms = chatRoomRepository.findByIsPrivateFalse();
            List<ChatRoom> userRooms = chatRoomRepository.findByMembersContaining(userId);
            
            return ResponseEntity.ok(Map.of(
                    "publicRooms", publicRooms,
                    "userRooms", userRooms
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable String roomId) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            
            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            
            return ResponseEntity.ok(Map.of(
                    "id", room.getId(),
                    "name", room.getName(),
                    "description", room.getDescription(),
                    "isPrivate", room.getIsPrivate(),
                    "owner", Map.of(
                            "id", room.getOwner().getId(),
                            "name", room.getOwner().getName(),
                            "picture", room.getOwner().getPicture()
                    ),
                    "createdAt", room.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @POST("/{roomId}/join")
    public ResponseEntity<?> joinRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable String roomId) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            
            room.getMembers().add(user);
            chatRoomRepository.save(room);
            
            return ResponseEntity.ok(Map.of("message", "Joined room successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @POST("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable String roomId) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            
            room.getMembers().remove(user);
            chatRoomRepository.save(room);
            
            return ResponseEntity.ok(Map.of("message", "Left room successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}