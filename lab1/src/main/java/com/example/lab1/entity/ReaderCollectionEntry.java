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
    name = "reader_collections",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reader_collections_entry",
        columnNames = {"username", "collection_name", "book_id"}
    )
)
public class ReaderCollectionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "collection_name", nullable = false, length = 40)
    private String collectionName;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ReaderCollectionEntry() {
    }

    public ReaderCollectionEntry(String username, String collectionName, Integer bookId) {
        this.username = username;
        this.collectionName = collectionName;
        this.bookId = bookId;
    }

    public Integer getId() { return id; }
    public String getCollectionName() { return collectionName; }
    public Integer getBookId() { return bookId; }
    public Instant getCreatedAt() { return createdAt; }
}
