package com.chatapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_members")
public class RoomMember {
    
    @EmbeddedId
    private RoomMemberId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private ChatRoom room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
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
        if (this.id == null) {
            this.id = new RoomMemberId();
        }
        this.id.setRoomId(room.getId());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (this.id == null) {
            this.id = new RoomMemberId();
        }
        this.id.setUserId(user.getId());
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}