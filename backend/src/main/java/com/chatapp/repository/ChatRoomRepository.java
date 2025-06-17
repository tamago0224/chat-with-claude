package com.chatapp.repository;

import com.chatapp.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isPrivate = false")
    Page<ChatRoom> findPublicRooms(Pageable pageable);
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members rm WHERE rm.user.id = :userId")
    List<ChatRoom> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.owner.id = :ownerId")
    List<ChatRoom> findByOwnerId(@Param("ownerId") String ownerId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isPrivate = false AND " +
           "(cr.name ILIKE %:searchTerm% OR cr.description ILIKE %:searchTerm%)")
    Page<ChatRoom> findPublicRoomsByNameOrDescriptionContainingIgnoreCase(
        @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members rm WHERE rm.user.id = :userId AND " +
           "(cr.name ILIKE %:searchTerm% OR cr.description ILIKE %:searchTerm%)")
    List<ChatRoom> findUserRoomsByNameOrDescriptionContainingIgnoreCase(
        @Param("userId") String userId, @Param("searchTerm") String searchTerm);
    
    boolean existsByName(String name);
}