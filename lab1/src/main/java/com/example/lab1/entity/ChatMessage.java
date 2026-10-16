package com.example.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String imageData;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ChatMessage() {
    }

    public ChatMessage(String username, String message, String imageData) {
        this.username = username;
        this.message = message;
        this.imageData = imageData;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public String getImageData() { return imageData; }
    public Instant getCreatedAt() { return createdAt; }
}
