package com.example.lab1.controller;

import com.example.lab1.entity.Comment;
import com.example.lab1.repository.CommentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/comments")
public class CommentController {
    private final CommentRepository comments;

    public CommentController(CommentRepository comments) { this.comments = comments; }

    @GetMapping
    public List<Comment> all(@PathVariable Integer bookId) { return comments.findByBookIdOrderByCreatedAtDesc(bookId); }

    @PostMapping
    public Comment add(@PathVariable Integer bookId, @RequestBody Comment comment) {
        comment.setId(null);
        comment.setBookId(bookId);
        comment.setCreatedAt(java.time.Instant.now());
        return comments.save(comment);
    }
}