package com.chatapp.socket;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.Message;
import com.chatapp.entity.User;
import com.chatapp.security.JwtUtil;
import com.chatapp.service.ChatRoomService;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SocketIOEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(SocketIOEventHandler.class);

  @Autowired private SocketIOServer socketIOServer;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserService userService;

  @Autowired private ChatRoomService chatRoomService;

  @Autowired private MessageService messageService;

  // Store connected clients
  private final Map<String, SocketIOClient> connectedClients = new ConcurrentHashMap<>();
  private final Map<String, String> userRooms = new ConcurrentHashMap<>();

  public void addEventListeners() {
    socketIOServer.addConnectListener(onConnected());
    socketIOServer.addDisconnectListener(onDisconnected());
    socketIOServer.addEventListener("join_room", JoinRoomData.class, onJoinRoom());
    socketIOServer.addEventListener("leave_room", LeaveRoomData.class, onLeaveRoom());
    socketIOServer.addEventListener("send_message", SendMessageData.class, onSendMessage());
    socketIOServer.addEventListener("typing", TypingData.class, onTyping());
  }

  private ConnectListener onConnected() {
    return client -> {
      String token = client.getHandshakeData().getSingleUrlParam("token");

      if (token == null || !jwtUtil.validateToken(token)) {
        logger.warn("Unauthorized connection attempt");
        client.disconnect();
        return;
      }

      String userId = jwtUtil.getUserIdFromToken(token);
      client.set("userId", userId);
      connectedClients.put(userId, client);

      logger.info("User {} connected", userId);

      // Notify user joined
      client.sendEvent("connected", Map.of("userId", userId));
    };
  }

  private DisconnectListener onDisconnected() {
    return client -> {
      String userId = client.get("userId");
      if (userId != null) {
        connectedClients.remove(userId);

        // Leave current room if any
        String currentRoom = userRooms.get(userId);
        if (currentRoom != null) {
          client.leaveRoom(currentRoom);
          userRooms.remove(userId);

          // Notify others in the room
          socketIOServer
              .getRoomOperations(currentRoom)
              .sendEvent("user_left", Map.of("userId", userId));
        }

        logger.info("User {} disconnected", userId);
      }
    };
  }

  private DataListener<JoinRoomData> onJoinRoom() {
    return (client, data, ackSender) -> {
      String userId = client.get("userId");
      String roomId = data.getRoomId();

      try {
        // Verify user has access to room
        ChatRoom room = chatRoomService.findById(roomId).orElse(null);
        if (room == null) {
          client.sendEvent("error", Map.of("message", "Room not found"));
          return;
        }

        // Check if user is member of the room
        if (!chatRoomService.isUserMemberOfRoom(userId, roomId)) {
          client.sendEvent("error", Map.of("message", "Access denied"));
          return;
        }

        // Leave previous room if any
        String previousRoom = userRooms.get(userId);
        if (previousRoom != null && !previousRoom.equals(roomId)) {
          client.leaveRoom(previousRoom);
          socketIOServer
              .getRoomOperations(previousRoom)
              .sendEvent("user_left", Map.of("userId", userId));
        }

        // Join new room
        client.joinRoom(roomId);
        userRooms.put(userId, roomId);

        User user = userService.findById(userId).orElse(null);
        if (user != null) {
          // Notify others in the room
          socketIOServer
              .getRoomOperations(roomId)
              .sendEvent(
                  "user_joined",
                  Map.of(
                      "userId", userId,
                      "userName", user.getName(),
                      "userPicture", user.getPicture()));
        }

        client.sendEvent("joined_room", Map.of("roomId", roomId));
        logger.info("User {} joined room {}", userId, roomId);

      } catch (Exception e) {
        logger.error("Error joining room", e);
        client.sendEvent("error", Map.of("message", "Failed to join room"));
      }
    };
  }

  private DataListener<LeaveRoomData> onLeaveRoom() {
    return (client, data, ackSender) -> {
      String userId = client.get("userId");
      String roomId = data.getRoomId();

      if (userRooms.get(userId) != null && userRooms.get(userId).equals(roomId)) {
        client.leaveRoom(roomId);
        userRooms.remove(userId);

        socketIOServer.getRoomOperations(roomId).sendEvent("user_left", Map.of("userId", userId));

        logger.info("User {} left room {}", userId, roomId);
      }
    };
  }

  private DataListener<SendMessageData> onSendMessage() {
    return (client, data, ackSender) -> {
      String userId = client.get("userId");
      String currentRoom = userRooms.get(userId);

      if (currentRoom == null) {
        client.sendEvent("error", Map.of("message", "Not in any room"));
        return;
      }

      try {
        User user = userService.findById(userId).orElse(null);
        ChatRoom room = chatRoomService.findById(currentRoom).orElse(null);

        if (user == null || room == null) {
          client.sendEvent("error", Map.of("message", "Invalid user or room"));
          return;
        }

        // Create and save message
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setRoom(room);
        message.setUser(user);
        message.setContent(data.getContent());
        message.setMessageType(Message.MessageType.valueOf(data.getType()));
        message.setImageUrl(data.getImageUrl());

        Message savedMessage = messageService.createMessage(message);

        // Broadcast message to all users in the room
        Map<String, Object> messageData =
            Map.of(
                "id", savedMessage.getId(),
                "roomId", currentRoom,
                "userId", userId,
                "userName", user.getName(),
                "userPicture", user.getPicture() != null ? user.getPicture() : "",
                "content", savedMessage.getContent(),
                "type", savedMessage.getMessageType().toString(),
                "imageUrl", savedMessage.getImageUrl() != null ? savedMessage.getImageUrl() : "",
                "timestamp", savedMessage.getCreatedAt().toString());

        socketIOServer.getRoomOperations(currentRoom).sendEvent("new_message", messageData);

        logger.info("Message sent by user {} in room {}", userId, currentRoom);

      } catch (Exception e) {
        logger.error("Error sending message", e);
        client.sendEvent("error", Map.of("message", "Failed to send message"));
      }
    };
  }

  private DataListener<TypingData> onTyping() {
    return (client, data, ackSender) -> {
      String userId = client.get("userId");
      String currentRoom = userRooms.get(userId);

      if (currentRoom != null) {
        User user = userService.findById(userId).orElse(null);
        if (user != null) {
          // Broadcast typing status to others in the room (excluding sender)
          socketIOServer
              .getRoomOperations(currentRoom)
              .sendEvent(
                  "user_typing",
                  Map.of(
                      "userId", userId,
                      "userName", user.getName(),
                      "typing", data.isTyping()),
                  client);
        }
      }
    };
  }

  // Data classes for socket events
  public static class JoinRoomData {
    private String roomId;

    public String getRoomId() {
      return roomId;
    }

    public void setRoomId(String roomId) {
      this.roomId = roomId;
    }
  }

  public static class LeaveRoomData {
    private String roomId;

    public String getRoomId() {
      return roomId;
    }

    public void setRoomId(String roomId) {
      this.roomId = roomId;
    }
  }

  public static class SendMessageData {
    private String content;
    private String type;
    private String imageUrl;

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getImageUrl() {
      return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
    }
  }

  public static class TypingData {
    private boolean typing;

    public boolean isTyping() {
      return typing;
    }

    public void setTyping(boolean typing) {
      this.typing = typing;
    }
  }
}
