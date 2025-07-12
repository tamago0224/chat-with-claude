package com.chatapp.repository;

import com.chatapp.entity.RoomMember;
import com.chatapp.entity.RoomMemberId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {

  @Query("SELECT rm FROM RoomMember rm WHERE rm.room.id = :roomId")
  List<RoomMember> findByRoomId(@Param("roomId") String roomId);

  @Query("SELECT rm FROM RoomMember rm WHERE rm.user.id = :userId")
  List<RoomMember> findByUserId(@Param("userId") String userId);

  @Query("SELECT COUNT(rm) FROM RoomMember rm WHERE rm.room.id = :roomId")
  long countByRoomId(@Param("roomId") String roomId);

  @Query("SELECT COUNT(rm) FROM RoomMember rm WHERE rm.user.id = :userId")
  long countByUserId(@Param("userId") String userId);

  @Query("SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.user.id = :userId")
  boolean existsByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") String userId);

  @Query("DELETE FROM RoomMember rm WHERE rm.room.id = :roomId")
  void deleteByRoomId(@Param("roomId") String roomId);
}
