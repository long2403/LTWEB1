package com.example.lab1.controller;

import com.example.lab1.entity.Comment;
import com.example.lab1.entity.ContentReport;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import com.example.lab1.repository.CommentRepository;
import com.example.lab1.repository.ContentReportRepository;
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
    private final ContentReportRepository reports;

    public AdminController(
        BookRepository books,
        ChapterRepository chapters,
        CommentRepository comments,
        ContentReportRepository reports
    ) {
        this.books = books;
        this.chapters = chapters;
        this.comments = comments;
        this.reports = reports;
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

    @GetMapping("/reports")
    public List<ContentReport> reports() {
        return reports.findAllByOrderByStatusAscCreatedAtDesc();
    }

    @org.springframework.web.bind.annotation.PostMapping("/reports/{id}/resolve")
    public ContentReport resolveReport(@PathVariable Integer id) {
        ContentReport report = reports.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Báo cáo không tồn tại.")
        );
        report.resolve();
        return reports.save(report);
    }
}
