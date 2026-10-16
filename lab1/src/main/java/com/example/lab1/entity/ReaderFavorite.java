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
    name = "reader_favorites",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reader_favorites_username_book",
        columnNames = {"username", "book_id"}
    )
)
public class ReaderFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ReaderFavorite() {
    }

    public ReaderFavorite(String username, Integer bookId) {
        this.username = username;
        this.bookId = bookId;
    }

    public String getUsername() { return username; }
    public Integer getBookId() { return bookId; }
    public Instant getCreatedAt() { return createdAt; }
}
