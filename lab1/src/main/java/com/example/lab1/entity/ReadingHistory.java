package com.example.lab1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "reading_history",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reading_history_username_book",
        columnNames = {"username", "book_id"}
    )
)
public class ReadingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected ReadingHistory() {
    }

    public ReadingHistory(String username, Integer bookId, Integer chapterNumber) {
        this.username = username;
        this.bookId = bookId;
        this.chapterNumber = chapterNumber;
    }

    public Integer getBookId() { return bookId; }
    public Integer getChapterNumber() { return chapterNumber; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateChapter(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
        this.updatedAt = Instant.now();
    }
}
