package com.chatapp.repository;

import com.chatapp.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

  @Query("SELECT m FROM Message m WHERE m.room.id = :roomId ORDER BY m.createdAt DESC")
  Page<Message> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") String roomId, Pageable pageable);

  @Query(
      "SELECT m FROM Message m WHERE m.room.id = :roomId AND m.createdAt > :since ORDER BY m.createdAt ASC")
  List<Message> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(
      @Param("roomId") String roomId, @Param("since") LocalDateTime since);

  @Query(
      "SELECT m FROM Message m WHERE m.room.id = :roomId AND m.content ILIKE %:searchTerm% ORDER BY m.createdAt DESC")
  Page<Message> findByRoomIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
      @Param("roomId") String roomId, @Param("searchTerm") String searchTerm, Pageable pageable);

  @Query("SELECT m FROM Message m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
  Page<Message> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

  @Query("SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId")
  long countByRoomId(@Param("roomId") String roomId);

  @Query("SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId AND m.createdAt > :since")
  long countByRoomIdAndCreatedAtAfter(
      @Param("roomId") String roomId, @Param("since") LocalDateTime since);

  @Query(
      "SELECT m FROM Message m WHERE m.room.id = :roomId AND m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt DESC")
  Page<Message> findByRoomIdAndCreatedAtBetweenOrderByCreatedAtDesc(
      @Param("roomId") String roomId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
