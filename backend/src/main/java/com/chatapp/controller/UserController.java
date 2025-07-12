package com.chatapp.controller;

import com.chatapp.entity.User;
import com.chatapp.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class UserController {

  @Autowired private UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable String id) {
    return userService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
    return userService
        .findByEmail(email)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<List<User>> getUsersByRoom(@PathVariable String roomId) {
    List<User> users = userService.findByRoomId(roomId);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/search")
  public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
    List<User> users = userService.searchUsers(q);
    return ResponseEntity.ok(users);
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userUpdate) {
    return userService
        .findById(id)
        .map(
            existingUser -> {
              if (userUpdate.getName() != null) {
                existingUser.setName(userUpdate.getName());
              }
              if (userUpdate.getPicture() != null) {
                existingUser.setPicture(userUpdate.getPicture());
              }
              User updatedUser = userService.updateUser(existingUser);
              return ResponseEntity.ok(updatedUser);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getUserStats() {
    long totalUsers = userService.getUserCount();

    Map<String, Object> stats = Map.of("totalUsers", totalUsers);

    return ResponseEntity.ok(stats);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    if (userService.findById(id).isPresent()) {
      userService.deleteUser(id);
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
