package com.chatapp.service;

import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findByRoomId(String roomId) {
        return userRepository.findByRoomId(roomId);
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByNameOrEmailContainingIgnoreCase(searchTerm);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public long getUserCount() {
        return userRepository.count();
    }
}