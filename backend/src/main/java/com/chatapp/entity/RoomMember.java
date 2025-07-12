package com.chatapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "room_members")
public class RoomMember {

  @EmbeddedId private RoomMemberId id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", insertable = false, updatable = false)
  private ChatRoom room;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @CreationTimestamp
  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  // Constructors
  public RoomMember() {}

  public RoomMember(ChatRoom room, User user) {
    this.room = room;
    this.user = user;
    this.id = new RoomMemberId(room.getId(), user.getId());
  }

  // Getters and Setters
  public RoomMemberId getId() {
    return id;
  }

  public void setId(RoomMemberId id) {
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

  public LocalDateTime getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(LocalDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }
}
