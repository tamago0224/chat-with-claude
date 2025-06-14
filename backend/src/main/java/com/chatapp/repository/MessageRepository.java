package com.chatapp.repository;

import com.chatapp.entity.Message;
import com.chatapp.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByRoomOrderByCreatedAtDesc(ChatRoom room, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.room = :room AND m.createdAt < :beforeTimestamp ORDER BY m.createdAt DESC")
    Page<Message> findByRoomAndCreatedAtBeforeOrderByCreatedAtDesc(
        @Param("room") ChatRoom room, 
        @Param("beforeTimestamp") LocalDateTime beforeTimestamp, 
        Pageable pageable
    );
    
    @Query("SELECT m FROM Message m WHERE m.room = :room AND m.content LIKE %:query% ORDER BY m.createdAt DESC")
    Page<Message> findByRoomAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
        @Param("room") ChatRoom room, 
        @Param("query") String query, 
        Pageable pageable
    );
}