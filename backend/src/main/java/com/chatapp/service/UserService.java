package com.chatapp.service;

import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getId())
        .password("") // パスワードは使用しない（JWT認証のため）
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }

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
