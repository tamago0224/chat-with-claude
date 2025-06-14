package com.chatapp.dto;

import com.chatapp.entity.Message;
import jakarta.validation.constraints.NotBlank;

public class SendMessageRequest {
    @NotBlank
    private String roomId;
    
    @NotBlank
    private String content;
    
    private Message.MessageType messageType = Message.MessageType.TEXT;
    
    private String imageUrl;

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Message.MessageType getMessageType() { return messageType; }
    public void setMessageType(Message.MessageType messageType) { this.messageType = messageType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}