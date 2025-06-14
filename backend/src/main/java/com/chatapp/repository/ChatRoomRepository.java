package com.chatapp.repository;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    List<ChatRoom> findByIsPrivateFalse();
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.id = :userId")
    List<ChatRoom> findByMembersContaining(@Param("userId") String userId);
    
    List<ChatRoom> findByOwner(User owner);
}