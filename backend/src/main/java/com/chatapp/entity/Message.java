package com.chatapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "messages")
public class Message {

  @Id
  @Column(name = "id", length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", length = 20)
  private MessageType messageType = MessageType.TEXT;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // Enum for message types
  public enum MessageType {
    TEXT,
    IMAGE,
    EMOJI
  }

  // Constructors
  public Message() {}

  public Message(
      String id,
      ChatRoom room,
      User user,
      String content,
      MessageType messageType,
      String imageUrl) {
    this.id = id;
    this.room = room;
    this.user = user;
    this.content = content;
    this.messageType = messageType;
    this.imageUrl = imageUrl;
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ChatRoom getRoom() {
    return room;
  }

  public void setRoom(ChatRoom room) {
    this.room = room;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
