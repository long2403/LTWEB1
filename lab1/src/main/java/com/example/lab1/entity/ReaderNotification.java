package com.example.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reader_notifications")
public class ReaderNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String recipient;

    @Column(nullable = false, length = 30)
    private String actor;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ReaderNotification() {
    }

    public ReaderNotification(
        String recipient,
        String actor,
        String type,
        String message,
        Integer bookId,
        Integer commentId
    ) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.bookId = bookId;
        this.commentId = commentId;
    }

    public Integer getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getActor() { return actor; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Integer getBookId() { return bookId; }
    public Integer getCommentId() { return commentId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
    public void markRead() { read = true; }
}
