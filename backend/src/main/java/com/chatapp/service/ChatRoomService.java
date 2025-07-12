package com.chatapp.service;

import com.chatapp.entity.ChatRoom;
import com.chatapp.entity.RoomMember;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.RoomMemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatRoomService {

  @Autowired private ChatRoomRepository chatRoomRepository;

  @Autowired private RoomMemberRepository roomMemberRepository;

  public ChatRoom createRoom(ChatRoom room) {
    return chatRoomRepository.save(room);
  }

  public ChatRoom updateRoom(ChatRoom room) {
    return chatRoomRepository.save(room);
  }

  public Optional<ChatRoom> findById(String id) {
    return chatRoomRepository.findById(id);
  }

  public List<ChatRoom> findByUserId(String userId) {
    return chatRoomRepository.findByUserId(userId);
  }

  public List<ChatRoom> findByOwnerId(String ownerId) {
    return chatRoomRepository.findByOwnerId(ownerId);
  }

  public Page<ChatRoom> findPublicRooms(Pageable pageable) {
    return chatRoomRepository.findPublicRooms(pageable);
  }

  public Page<ChatRoom> searchPublicRooms(String searchTerm, Pageable pageable) {
    return chatRoomRepository.findPublicRoomsByNameOrDescriptionContainingIgnoreCase(
        searchTerm, pageable);
  }

  public List<ChatRoom> searchUserRooms(String userId, String searchTerm) {
    return chatRoomRepository.findUserRoomsByNameOrDescriptionContainingIgnoreCase(
        userId, searchTerm);
  }

  public boolean existsByName(String name) {
    return chatRoomRepository.existsByName(name);
  }

  public void deleteRoom(String id) {
    chatRoomRepository.deleteById(id);
  }

  public long getRoomCount() {
    return chatRoomRepository.count();
  }

  // Room membership methods
  public boolean isUserMemberOfRoom(String userId, String roomId) {
    return roomMemberRepository.existsByRoomIdAndUserId(roomId, userId);
  }

  public void addUserToRoom(String userId, String roomId) {
    if (!isUserMemberOfRoom(userId, roomId)) {
      RoomMember roomMember = new RoomMember();
      roomMember.setId(new com.chatapp.entity.RoomMemberId(roomId, userId));
      roomMemberRepository.save(roomMember);
    }
  }

  public void removeUserFromRoom(String userId, String roomId) {
    roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
  }

  public List<RoomMember> getRoomMembers(String roomId) {
    return roomMemberRepository.findByRoomId(roomId);
  }

  public long getRoomMemberCount(String roomId) {
    return roomMemberRepository.countByRoomId(roomId);
  }
}
