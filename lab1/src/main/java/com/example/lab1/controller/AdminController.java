package com.example.lab1.controller;

import com.example.lab1.entity.Comment;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.CommentRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final BookRepository books;
    private final ChapterRepository chapters;
    private final CommentRepository comments;

    public AdminController(BookRepository books, ChapterRepository chapters, CommentRepository comments) {
        this.books = books;
        this.chapters = chapters;
        this.comments = comments;
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        return Map.of(
            "books", books.count(),
            "chapters", chapters.count(),
            "comments", comments.count(),
            "views", books.sumViews()
        );
    }

    @GetMapping("/comments")
    public List<Comment> comments() {
        return comments.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable Integer id) {
        Comment comment = comments.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bình luận không tồn tại.")
        );
        if (!comment.isDeleted()) {
            comment.setDeleted(true);
            comment.setContent("");
            comments.save(comment);
        }
    }
}
