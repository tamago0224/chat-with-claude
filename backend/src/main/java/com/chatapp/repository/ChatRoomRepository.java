package com.chatapp.repository;

import com.chatapp.entity.ChatRoom;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

  @Query("SELECT cr FROM ChatRoom cr WHERE cr.isPrivate = false")
  Page<ChatRoom> findPublicRooms(Pageable pageable);

  @Query("SELECT cr FROM ChatRoom cr JOIN RoomMember rm ON cr.id = rm.id.roomId WHERE rm.id.userId = :userId")
  List<ChatRoom> findByUserId(@Param("userId") String userId);

  @Query("SELECT cr FROM ChatRoom cr WHERE cr.owner.id = :ownerId")
  List<ChatRoom> findByOwnerId(@Param("ownerId") String ownerId);

  @Query(
      "SELECT cr FROM ChatRoom cr WHERE cr.isPrivate = false AND "
          + "(cr.name ILIKE %:searchTerm% OR cr.description ILIKE %:searchTerm%)")
  Page<ChatRoom> findPublicRoomsByNameOrDescriptionContainingIgnoreCase(
      @Param("searchTerm") String searchTerm, Pageable pageable);

  @Query(
      "SELECT cr FROM ChatRoom cr JOIN RoomMember rm ON cr.id = rm.id.roomId WHERE rm.id.userId = :userId AND "
          + "(cr.name ILIKE %:searchTerm% OR cr.description ILIKE %:searchTerm%)")
  List<ChatRoom> findUserRoomsByNameOrDescriptionContainingIgnoreCase(
      @Param("userId") String userId, @Param("searchTerm") String searchTerm);

  boolean existsByName(String name);
}
