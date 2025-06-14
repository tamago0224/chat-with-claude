package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRoomRequest {
    @NotBlank
    private String name;
    
    private String description;
    
    private Boolean isPrivate = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }
}