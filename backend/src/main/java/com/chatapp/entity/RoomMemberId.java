package com.chatapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoomMemberId implements Serializable {

  @Column(name = "room_id", length = 36)
  private String roomId;

  @Column(name = "user_id", length = 36)
  private String userId;

  // Constructors
  public RoomMemberId() {}

  public RoomMemberId(String roomId, String userId) {
    this.roomId = roomId;
    this.userId = userId;
  }

  // Getters and Setters
  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoomMemberId that = (RoomMemberId) o;
    return Objects.equals(roomId, that.roomId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomId, userId);
  }
}
