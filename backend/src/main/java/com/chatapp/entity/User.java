package com.chatapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "members")
    private Set<ChatRoom> rooms;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Message> messages;

    public User() {}

    public User(String id, String email, String name, String picture, String googleId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.googleId = googleId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<ChatRoom> getRooms() { return rooms; }
    public void setRooms(Set<ChatRoom> rooms) { this.rooms = rooms; }

    public Set<Message> getMessages() { return messages; }
    public void setMessages(Set<Message> messages) { this.messages = messages; }
}