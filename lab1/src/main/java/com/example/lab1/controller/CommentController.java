package com.example.lab1.controller;

import com.example.lab1.entity.Comment;
import com.example.lab1.repository.CommentRepository;
import com.example.lab1.repository.BookRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
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
    private final BookRepository books;

    public CommentController(CommentRepository comments, BookRepository books) {
        this.comments = comments;
        this.books = books;
    }

    @GetMapping
    public List<Comment> all(@PathVariable Integer bookId) { return comments.findByBookIdOrderByCreatedAtDesc(bookId); }

    @PostMapping
    public Comment add(
        @PathVariable Integer bookId,
        @Valid @RequestBody CommentRequest request,
        Authentication authentication
    ) {
        if (!books.existsById(bookId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Truyện không tồn tại."
            );
        }
        Comment comment = new Comment();
        comment.setBookId(bookId);
        comment.setUsername(authentication.getName());
        comment.setContent(request.content().trim());
        comment.setRating(request.rating());
        return comments.save(comment);
    }

    public record CommentRequest(@NotBlank @Size(max = 2000) String content, @Min(1) @Max(5) Integer rating) {
    }
}