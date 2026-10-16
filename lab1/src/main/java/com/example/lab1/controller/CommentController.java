package com.example.lab1.controller;

import com.example.lab1.entity.Comment;
import com.example.lab1.repository.CommentRepository;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.entity.ReaderNotification;
import com.example.lab1.repository.ReaderNotificationRepository;
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
    private final ReaderNotificationRepository notifications;

    public CommentController(
        CommentRepository comments,
        BookRepository books,
        ReaderNotificationRepository notifications
    ) {
        this.comments = comments;
        this.books = books;
        this.notifications = notifications;
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
        Comment parent = null;
        if (request.parentCommentId() != null) {
            parent = comments.findById(request.parentCommentId()).orElseThrow(
                () -> new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Bình luận được trả lời không tồn tại."
                )
            );
            if (!parent.getBookId().equals(bookId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Bình luận trả lời phải thuộc cùng bộ truyện."
                );
            }
        }
        Comment comment = new Comment();
        comment.setBookId(bookId);
        comment.setUsername(authentication.getName());
        comment.setContent(request.content().trim());
        comment.setRating(request.rating());
        comment.setParentCommentId(request.parentCommentId());
        Comment saved = comments.save(comment);
        if (parent != null && !parent.getUsername().equalsIgnoreCase(authentication.getName())) {
            String storyTitle = books.findById(bookId).orElseThrow().getTitle();
            notifications.save(new ReaderNotification(
                parent.getUsername(),
                authentication.getName(),
                "COMMENT_REPLY",
                authentication.getName() + " đã phản hồi bình luận của bạn trong " + storyTitle,
                bookId,
                saved.getId()
            ));
        }
        return saved;
    }

    public record CommentRequest(
        @NotBlank @Size(max = 2000) String content,
        @Min(1) @Max(5) Integer rating,
        @jakarta.validation.constraints.Positive Integer parentCommentId
    ) {
    }
}