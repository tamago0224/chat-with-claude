package com.chatapp.repository;

import com.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByGoogleId(String googleId);
    
    @Query("SELECT u FROM User u JOIN u.roomMemberships rm WHERE rm.room.id = :roomId")
    List<User> findByRoomId(@Param("roomId") String roomId);
    
    @Query("SELECT u FROM User u WHERE u.name ILIKE %:searchTerm% OR u.email ILIKE %:searchTerm%")
    List<User> findByNameOrEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    boolean existsByEmail(String email);
    
    boolean existsByGoogleId(String googleId);
}