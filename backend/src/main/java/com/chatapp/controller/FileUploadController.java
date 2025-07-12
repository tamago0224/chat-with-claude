package com.chatapp.controller;

import com.chatapp.security.JwtUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class FileUploadController {

  @Autowired private JwtUtil jwtUtil;

  private final Path uploadLocation;

  public FileUploadController() {
    try {
      // Docker環境では/tmp/uploadsを使用（権限問題を回避）
      String uploadDir = System.getProperty("java.io.tmpdir") + "/uploads";
      uploadLocation = Paths.get(uploadDir);
      Files.createDirectories(uploadLocation);
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory", e);
    }
  }

  @PostMapping("/image")
  public ResponseEntity<Map<String, String>> uploadImage(
      @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Validate file
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
    }

    // Check file size (max 10MB)
    if (file.getSize() > 10 * 1024 * 1024) {
      return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 10MB limit"));
    }

    // Check file type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
    }

    // Allowed image types
    if (!contentType.equals("image/jpeg")
        && !contentType.equals("image/png")
        && !contentType.equals("image/gif")
        && !contentType.equals("image/webp")) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Unsupported image format. Allowed: JPEG, PNG, GIF, WebP"));
    }

    try {
      // Generate unique filename
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }
      String filename = UUID.randomUUID().toString() + extension;

      // Save file
      Path targetLocation = uploadLocation.resolve(filename);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      // Return file URL
      String fileUrl = "/api/files/" + filename;
      return ResponseEntity.ok(
          Map.of(
              "url", fileUrl,
              "filename", filename,
              "originalName", originalFilename != null ? originalFilename : "",
              "size", String.valueOf(file.getSize()),
              "contentType", contentType));

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to upload file"));
    }
  }

  @GetMapping("/files/{filename}")
  public ResponseEntity<Resource> getFile(@PathVariable String filename) {
    try {
      Path filePath = uploadLocation.resolve(filename).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        // Determine content type
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
          contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (MalformedURLException e) {
      return ResponseEntity.badRequest().build();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/files/{filename}")
  public ResponseEntity<Map<String, String>> deleteFile(
      @PathVariable String filename, @RequestHeader("Authorization") String token) {
    String userId = getUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      Path filePath = uploadLocation.resolve(filename).normalize();

      if (Files.exists(filePath)) {
        Files.delete(filePath);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to delete file"));
    }
  }

  private String getUserIdFromToken(String token) {
    if (token == null || !token.startsWith("Bearer ")) {
      return null;
    }

    String jwtToken = token.substring(7);
    if (!jwtUtil.validateToken(jwtToken)) {
      return null;
    }

    return jwtUtil.getUserIdFromToken(jwtToken);
  }
}
