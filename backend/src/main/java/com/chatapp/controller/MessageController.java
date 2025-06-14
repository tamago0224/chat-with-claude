package com.chatapp.controller;

import com.chatapp.dto.SendMessageRequest;
import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.Message;
import com.chatapp.entity.User;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody SendMessageRequest request) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            Message message = new Message();
            message.setId(UUID.randomUUID().toString());
            message.setRoom(room);
            message.setUser(user);
            message.setContent(request.getContent());
            message.setMessageType(request.getMessageType());
            message.setImageUrl(request.getImageUrl());
            
            message = messageRepository.save(message);
            
            return ResponseEntity.ok(Map.of(
                    "id", message.getId(),
                    "content", message.getContent(),
                    "messageType", message.getMessageType(),
                    "imageUrl", message.getImageUrl(),
                    "user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "picture", user.getPicture()
                    ),
                    "createdAt", message.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String beforeTimestamp) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            
            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messagesPage;
            
            if (beforeTimestamp != null) {
                LocalDateTime timestamp = LocalDateTime.parse(beforeTimestamp);
                messagesPage = messageRepository.findByRoomAndCreatedAtBeforeOrderByCreatedAtDesc(
                        room, timestamp, pageable);
            } else {
                messagesPage = messageRepository.findByRoomOrderByCreatedAtDesc(room, pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                    "messages", messagesPage.getContent().stream().map(message -> Map.of(
                            "id", message.getId(),
                            "content", message.getContent(),
                            "messageType", message.getMessageType(),
                            "imageUrl", message.getImageUrl(),
                            "user", Map.of(
                                    "id", message.getUser().getId(),
                                    "name", message.getUser().getName(),
                                    "picture", message.getUser().getPicture()
                            ),
                            "createdAt", message.getCreatedAt()
                    )).collect(Collectors.toList()),
                    "totalPages", messagesPage.getTotalPages(),
                    "totalElements", messagesPage.getTotalElements(),
                    "hasNext", messagesPage.hasNext()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}/search")
    public ResponseEntity<?> searchMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable String roomId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String userId = jwtService.extractUserId(token.replace("Bearer ", ""));
            
            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messagesPage = messageRepository
                    .findByRoomAndContentContainingIgnoreCaseOrderByCreatedAtDesc(room, query, pageable);
            
            return ResponseEntity.ok(Map.of(
                    "messages", messagesPage.getContent().stream().map(message -> Map.of(
                            "id", message.getId(),
                            "content", message.getContent(),
                            "messageType", message.getMessageType(),
                            "imageUrl", message.getImageUrl(),
                            "user", Map.of(
                                    "id", message.getUser().getId(),
                                    "name", message.getUser().getName(),
                                    "picture", message.getUser().getPicture()
                            ),
                            "createdAt", message.getCreatedAt()
                    )).collect(Collectors.toList()),
                    "totalPages", messagesPage.getTotalPages(),
                    "totalElements", messagesPage.getTotalElements(),
                    "hasNext", messagesPage.hasNext()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}