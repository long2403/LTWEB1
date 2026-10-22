package com.example.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "content_reports")
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "chapter_number")
    private Integer chapterNumber;

    @Column(nullable = false, length = 40)
    private String reason;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant resolvedAt;

    protected ContentReport() {
    }

    public ContentReport(String username, Integer bookId, Integer chapterNumber, String reason, String details) {
        this.username = username;
        this.bookId = bookId;
        this.chapterNumber = chapterNumber;
        this.reason = reason;
        this.details = details;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public Integer getBookId() { return bookId; }
    public Integer getChapterNumber() { return chapterNumber; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void resolve() {
        status = "RESOLVED";
        resolvedAt = Instant.now();
    }
}
