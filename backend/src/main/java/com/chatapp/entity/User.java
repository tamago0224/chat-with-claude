package com.chatapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

  @Id
  @Column(name = "id", length = 36)
  private String id;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "picture", length = 500)
  private String picture;

  @JsonIgnore
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Room memberships
  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<RoomMember> roomMemberships = new HashSet<>();

  // Owned rooms
  @JsonIgnore
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<ChatRoom> ownedRooms = new HashSet<>();

  // Messages
  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Message> messages = new HashSet<>();

  // Constructors
  public User() {}

  public User(String id, String email, String name, String picture, String passwordHash) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.picture = picture;
    this.passwordHash = passwordHash;
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<RoomMember> getRoomMemberships() {
    return roomMemberships;
  }

  public void setRoomMemberships(Set<RoomMember> roomMemberships) {
    this.roomMemberships = roomMemberships;
  }

  public Set<ChatRoom> getOwnedRooms() {
    return ownedRooms;
  }

  public void setOwnedRooms(Set<ChatRoom> ownedRooms) {
    this.ownedRooms = ownedRooms;
  }

  public Set<Message> getMessages() {
    return messages;
  }

  public void setMessages(Set<Message> messages) {
    this.messages = messages;
  }
}
