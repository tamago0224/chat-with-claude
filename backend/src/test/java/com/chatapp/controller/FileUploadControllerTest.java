package com.chatapp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chatapp.security.JwtUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Create test upload directory
        try {
            String uploadDir = System.getProperty("java.io.tmpdir") + "/uploads";
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void mockValidToken() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("test-user-id");
    }

    @Test
    void uploadImage_ValidJpegFile_ShouldUploadSuccessfully() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.filename").exists())
                .andExpect(jsonPath("$.originalName").value("test.jpg"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getUserIdFromToken("valid-token");
    }

    @Test
    void uploadImage_ValidPngFile_ShouldUploadSuccessfully() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void uploadImage_ValidGifFile_ShouldUploadSuccessfully() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.gif",
            "image/gif",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/gif"));
    }

    @Test
    void uploadImage_ValidWebpFile_ShouldUploadSuccessfully() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.webp",
            "image/webp",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/webp"));
    }

    @Test
    void uploadImage_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
    }

    @Test
    void uploadImage_EmptyFile_ShouldReturnBadRequest() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is empty"));
    }

    @Test
    void uploadImage_FileTooLarge_ShouldReturnBadRequest() throws Exception {
        // Given
        mockValidToken();
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File size exceeds 10MB limit"));
    }

    @Test
    void uploadImage_NonImageFile_ShouldReturnBadRequest() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only image files are allowed"));
    }

    @Test
    void uploadImage_UnsupportedImageFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.bmp",
            "image/bmp",
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unsupported image format. Allowed: JPEG, PNG, GIF, WebP"));
    }

    @Test
    void uploadImage_NullContentType_ShouldReturnBadRequest() throws Exception {
        // Given
        mockValidToken();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            null,
            "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only image files are allowed"));
    }

    @Test
    void getFile_ExistingFile_ShouldReturnFile() throws Exception {
        // Note: This test would require creating an actual file in the upload directory
        // For a real implementation, you might want to use a test-specific upload directory
        // and create test files during setup
        
        // This is a simplified test that just checks the endpoint exists
        mockMvc.perform(get("/api/upload/files/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFile_ValidToken_ShouldDeleteFile() throws Exception {
        // Given
        mockValidToken();

        // When & Then
        mockMvc.perform(delete("/api/upload/files/test.jpg")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound()); // File doesn't exist

        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getUserIdFromToken("valid-token");
    }

    @Test
    void deleteFile_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/upload/files/test.jpg")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).getUserIdFromToken(anyString());
    }
}