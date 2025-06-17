package com.chatapp.service;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public Optional<Message> findById(String id) {
        return messageRepository.findById(id);
    }

    public Page<Message> findByRoomId(String roomId, Pageable pageable) {
        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    public List<Message> findRecentMessages(String roomId, LocalDateTime since) {
        return messageRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(roomId, since);
    }

    public Page<Message> searchMessages(String roomId, String searchTerm, Pageable pageable) {
        return messageRepository.findByRoomIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(roomId, searchTerm, pageable);
    }

    public Page<Message> findByUserId(String userId, Pageable pageable) {
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getMessageCount(String roomId) {
        return messageRepository.countByRoomId(roomId);
    }

    public long getRecentMessageCount(String roomId, LocalDateTime since) {
        return messageRepository.countByRoomIdAndCreatedAtAfter(roomId, since);
    }

    public Page<Message> findMessagesByDateRange(String roomId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return messageRepository.findByRoomIdAndCreatedAtBetweenOrderByCreatedAtDesc(roomId, startDate, endDate, pageable);
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
    }

    public long getTotalMessageCount() {
        return messageRepository.count();
    }
}